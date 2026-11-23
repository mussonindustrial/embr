import * as React from 'react'
import { messageOf } from '@embr-js/utils'
import ts from 'typescript'
import {
  componentOf,
  createPeriscopeApi,
  describeComponent,
  ProjectComponent,
} from '@embr-modules/periscope-web/api'
import { isResource, ProjectFile } from './files'
import {
  ComponentFailure,
  ComponentsRequest,
  ComponentsResult,
  ComponentsRevision,
} from './protocol'
import { files } from './workspace'

type Module = { exports: Record<string, unknown> }

type LoadedModule = {
  module: Module
  file: ProjectFile | undefined
  imports: Set<string>
  error?: string
}

type Evaluation = Omit<ComponentsResult, 'revision'>

type Description = ReturnType<typeof describeComponent>

type CachedEvaluation = {
  signature: string
  componentsKey: string
  result: ComponentsResult
}

const RESOURCE_IMPORT = '@/'
const RELATIVE_IMPORT = /^\.\.?\//
const CSS_DECLARATION = '.d.ts'

const placeholder: unknown = new Proxy(function () {}, {
  get: (_target, key) => {
    if (key === Symbol.toPrimitive) return () => ''
    if (key === 'then') return undefined
    return placeholder
  },
  apply: () => placeholder,
  construct: () => placeholder as object,
})

const classNames = new Proxy(
  {},
  { get: (_target, key) => (typeof key === 'string' ? key : undefined) }
)

const compilerOptions: ts.CompilerOptions = {
  module: ts.ModuleKind.CommonJS,
  target: ts.ScriptTarget.ES2020,
  jsx: ts.JsxEmit.React,
  esModuleInterop: true,
}

const loadedModules = new Map<string, LoadedModule>()
const transpiledOutputs = new WeakMap<ProjectFile, string>()
const descriptions = new WeakMap<ProjectComponent, Description>()

let executingPath: string | undefined
let lastEvaluation: CachedEvaluation | undefined

const api = createPeriscopeApi({
  resource: async (path) => loadModule(path).module.exports as never,
  source: () => executingPath,
})

function resourcePathOf(specifier: string): string | undefined {
  if (specifier.startsWith(RESOURCE_IMPORT)) {
    return specifier.slice(RESOURCE_IMPORT.length)
  }
  if (RELATIVE_IMPORT.test(specifier) && executingPath != null) {
    const { pathname } = new URL(specifier, `file:///${executingPath}`)
    return decodeURI(pathname.slice(1))
  }
  return undefined
}

function importModule(specifier: string): unknown {
  if (specifier === 'react') return React
  if (specifier === 'system:periscope') return api

  const path = resourcePathOf(specifier)
  return path == null ? placeholder : loadModule(path).module.exports
}

function transpile(file: ProjectFile, fileName: string) {
  const cached = transpiledOutputs.get(file)
  if (cached != null) return cached

  const { outputText } = ts.transpileModule(file.text, {
    fileName,
    compilerOptions,
  })
  transpiledOutputs.set(file, outputText)
  return outputText
}

function execute(path: string, loaded: LoadedModule, output: string) {
  const previousPath = executingPath
  executingPath = path
  try {
    const run = new Function('require', 'module', 'exports', output)
    run(importModule, loaded.module, loaded.module.exports)
  } catch (error) {
    loaded.error = messageOf(error)
  } finally {
    executingPath = previousPath
  }
}

function loadModule(path: string): LoadedModule {
  if (executingPath != null) {
    loadedModules.get(executingPath)?.imports.add(path)
  }

  const cached = loadedModules.get(path)
  if (cached != null) return cached

  const fileName = files.fileNameOf(path)
  const file = files.get(fileName)
  const loaded: LoadedModule = {
    module: { exports: {} },
    file,
    imports: new Set(),
  }
  loadedModules.set(path, loaded)

  if (file == null) {
    loaded.module.exports = placeholder as Module['exports']
  } else if (fileName.endsWith(CSS_DECLARATION)) {
    loaded.module.exports = { __esModule: true, default: classNames }
  } else {
    execute(path, loaded, transpile(file, fileName))
  }
  return loaded
}

function removeUnreachableModules(roots: string[]) {
  const reached = new Set<string>()
  const visit = (path: string) => {
    if (reached.has(path)) return
    reached.add(path)
    loadedModules.get(path)?.imports.forEach(visit)
  }
  roots.forEach(visit)

  loadedModules.forEach((_loaded, path) => {
    if (!reached.has(path)) loadedModules.delete(path)
  })
}

function moduleFailures(): ComponentFailure[] {
  const failures: ComponentFailure[] = []
  loadedModules.forEach(({ error }, path) => {
    if (error != null) failures.push({ path, message: error })
  })
  return failures
}

function removeChangedModules() {
  const removed = new Set<string>()
  const isRemoved = (path: string) =>
    removed.has(path) || !loadedModules.has(path)
  const isChanged = (loaded: LoadedModule, path: string) =>
    loaded.file !== files.get(files.fileNameOf(path)) ||
    [...loaded.imports].some(isRemoved)

  let foundChanges = true
  while (foundChanges) {
    foundChanges = false
    loadedModules.forEach((loaded, path) => {
      if (removed.has(path) || !isChanged(loaded, path)) return
      removed.add(path)
      foundChanges = true
    })
  }

  removed.forEach((path) => loadedModules.delete(path))
  return removed.size
}

function workspaceSignature() {
  return files.fileNames
    .filter(isResource)
    .sort()
    .map((fileName) => `${fileName}:${files.get(fileName)?.version}`)
    .concat(files.componentPaths)
    .join('|')
}

function descriptionOf(component: ProjectComponent) {
  const cached = descriptions.get(component)
  if (cached != null) return cached

  const description = describeComponent(component)
  descriptions.set(component, description)
  return description
}

function evaluateComponents(paths: string[]): Evaluation {
  const failures: ComponentFailure[] = []

  const components = paths.flatMap((path) => {
    const loaded = loadModule(path)
    if (loaded.error != null) return []

    try {
      const component = componentOf(loaded.module.exports)
      return [{ ...descriptionOf(component), path }]
    } catch (error) {
      failures.push({ path, message: messageOf(error) })
      return []
    }
  })

  removeUnreachableModules(paths)
  failures.push(...moduleFailures())

  return { components, failures }
}

function evaluate(): ComponentsResult {
  const currentSignature = workspaceSignature()
  if (lastEvaluation?.signature === currentSignature) {
    return lastEvaluation.result
  }

  const components = [...files.componentPaths].sort()
  const componentsKey = components.join('|')
  const removedCount = removeChangedModules()

  if (
    lastEvaluation != null &&
    removedCount === 0 &&
    lastEvaluation.componentsKey === componentsKey
  ) {
    lastEvaluation = { ...lastEvaluation, signature: currentSignature }
    return lastEvaluation.result
  }

  const revision = (lastEvaluation?.result.revision ?? 0) + 1
  lastEvaluation = {
    signature: currentSignature,
    componentsKey,
    result: { revision, ...evaluateComponents(components) },
  }
  return lastEvaluation.result
}

export function evaluationFailuresOf(path: string): ComponentFailure[] {
  const failures = lastEvaluation?.result.failures ?? []
  return failures.filter((failure) => failure.path === path)
}

export async function describeComponents({
  revision,
}: ComponentsRequest): Promise<ComponentsResult | ComponentsRevision> {
  const result = evaluate()
  if (revision === result.revision) return { revision }
  return result
}
