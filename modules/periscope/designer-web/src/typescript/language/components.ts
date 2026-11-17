import * as React from 'react'
import ts from 'typescript'
import {
  createPeriscopeApi,
  LibraryComponent,
  describeComponent,
  keyOf,
  libraryOf,
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
  libraries: string
  result: ComponentsResult
}

const RESOURCE_IMPORT = '@/'
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
const descriptions = new WeakMap<LibraryComponent, Description>()

let executingPath: string | undefined
let lastEvaluation: CachedEvaluation | undefined

const api = createPeriscopeApi({
  resource: async (path) => loadModule(path).module.exports as never,
  source: () => executingPath,
})

function messageOf(error: unknown) {
  return String((error as Error)?.message ?? error)
}

function fileOf(path: string) {
  return files.get(files.fileNameOf(path))
}

function importModule(specifier: string): unknown {
  if (specifier === 'react') return React
  if (specifier === 'system:periscope') return api
  if (specifier.startsWith(RESOURCE_IMPORT)) {
    const path = specifier.slice(RESOURCE_IMPORT.length)
    return loadModule(path).module.exports
  }
  return placeholder
}

function transpile(file: ProjectFile, fileName: string) {
  const known = transpiledOutputs.get(file)
  if (known != null) return known

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

  const known = loadedModules.get(path)
  if (known != null) return known

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
    loaded.file !== fileOf(path) || [...loaded.imports].some(isRemoved)

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

function signature() {
  return files.fileNames
    .filter(isResource)
    .sort()
    .map((fileName) => `${fileName}:${files.get(fileName)?.version}`)
    .concat(files.libraries)
    .join('|')
}

function descriptionOf(component: LibraryComponent) {
  const known = descriptions.get(component)
  if (known != null) return known

  const description = describeComponent(component)
  descriptions.set(component, description)
  return description
}

function duplicateDiagnostic(key: string, source: string) {
  return `'${key}' is defined more than once (also in '${source}'). The first definition is used.`
}

function evaluateLibraries(libraries: string[]): Evaluation {
  const components = new Map<string, LibraryComponent>()
  const duplicates = new Map<string, string[]>()
  const failures: ComponentFailure[] = []

  const addComponent = (
    key: string,
    component: LibraryComponent,
    library: string
  ) => {
    const existing = components.get(key)
    if (existing == null) {
      components.set(key, component)
      return
    }
    if (existing === component) return

    const source = component.periscope.source ?? library
    const diagnostics = duplicates.get(key) ?? []
    duplicates.set(key, [...diagnostics, duplicateDiagnostic(key, source)])
  }

  libraries.forEach((library) => {
    const loaded = loadModule(library)
    if (loaded.error != null) return

    try {
      const { namespace, components: listed } = libraryOf(loaded.module.exports)
      listed.forEach((component) => {
        const key = keyOf(namespace, component.periscope.id)
        addComponent(key, component, library)
      })
    } catch (error) {
      failures.push({ path: library, message: messageOf(error) })
    }
  })

  removeUnreachableModules(libraries)
  failures.push(...moduleFailures())

  const described = [...components].map(([key, component]) => {
    const description = descriptionOf(component)
    const diagnostics = [
      ...description.diagnostics,
      ...(duplicates.get(key) ?? []),
    ]
    return { ...description, id: key, diagnostics }
  })

  return { components: described, failures }
}

function evaluate(): ComponentsResult {
  const currentSignature = signature()
  if (lastEvaluation?.signature === currentSignature) {
    return lastEvaluation.result
  }

  const libraries = [...files.libraries].sort()
  const librariesKey = libraries.join('|')
  const removedCount = removeChangedModules()

  if (
    lastEvaluation != null &&
    removedCount === 0 &&
    lastEvaluation.libraries === librariesKey
  ) {
    lastEvaluation = { ...lastEvaluation, signature: currentSignature }
    return lastEvaluation.result
  }

  const revision = (lastEvaluation?.result.revision ?? 0) + 1
  lastEvaluation = {
    signature: currentSignature,
    libraries: librariesKey,
    result: { revision, ...evaluateLibraries(libraries) },
  }
  return lastEvaluation.result
}

export function evaluationFailuresOf(path: string): ComponentFailure[] {
  const failures = lastEvaluation?.result.failures ?? []
  return failures.filter((failure) => failure.path === path)
}

export async function describeComponents(): Promise<ComponentsResult>
export async function describeComponents(
  request: ComponentsRequest
): Promise<ComponentsResult | ComponentsRevision>
export async function describeComponents(
  request: ComponentsRequest = {}
): Promise<ComponentsResult | ComponentsRevision> {
  const result = evaluate()
  if (request.revision === result.revision) {
    return { revision: result.revision }
  }
  return result
}
