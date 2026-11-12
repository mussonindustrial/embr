import { readFileSync } from 'fs'
import { basename, join } from 'path'
import ts from 'typescript'
import { TYPINGS_ROOT } from '../../src/typescript/language/files'
import type { Typings } from '../../src/typescript/language/protocol'
import { compactTypings } from './compactTypings'
import { systemModuleFiles } from './systemModules'

export type TypingsPackage = {
  name: string
  docs?: boolean
}

export type TypingsManifest = {
  libs: string[]
  packages: TypingsPackage[]
  globals: Record<string, string>
  systemModules: Record<string, string>
}

const NODE_MODULES = '/node_modules/'

const options: ts.CompilerOptions = {
  module: ts.ModuleKind.ESNext,
  moduleResolution: ts.ModuleResolutionKind.Bundler,
  types: [],
  noEmit: true,
}

export function collectTypings(
  manifest: TypingsManifest,
  projectRoot: string
): Typings {
  const projectFile = join(projectRoot, 'package.json')
  const packages = new Map(manifest.packages.map((pkg) => [pkg.name, pkg]))
  const files = new Map<string, string>()

  const putFirst = (path: string, text: () => string) => {
    if (!files.has(path)) files.set(path, text())
  }

  const resolve = (specifier: string, from: string) => {
    const { resolvedModule } = ts.resolveModuleName(
      specifier,
      from,
      options,
      ts.sys
    )
    if (resolvedModule == null) {
      throw new Error(`Can't resolve '${specifier}' from '${from}'.`)
    }
    return normalize(resolvedModule.resolvedFileName)
  }

  const globalEntries = Object.entries(manifest.globals).map(
    ([pkg, global]) => {
      const entry = resolve(pkg, projectFile)
      if (!packages.has(packageNameOf(entry))) {
        throw new Error(`Package '${pkg}' isn't in the typings manifest.`)
      }
      if (ts.sys.readFile(entry)?.includes(`export as namespace ${global}`)) {
        return { entry, root: virtualPath(entry) }
      }
      const root = `${TYPINGS_ROOT}globals/${global}.d.ts`
      files.set(root, `export * from '${pkg}'\nexport as namespace ${global}\n`)
      return { entry, root }
    }
  )

  const systemEntries = systemModuleFiles(
    manifest.systemModules,
    projectRoot
  ).flatMap(({ path, text, source }) => {
    files.set(path, compactTypings(text, true))
    return ts
      .preProcessFile(text, true, true)
      .importedFiles.filter(({ fileName }) => !fileName.startsWith('.'))
      .map(({ fileName }) => resolve(fileName, source))
  })
  systemEntries
    .map(packageNameOf)
    .filter((name) => !packages.has(name))
    .forEach((name) => packages.set(name, { name }))

  const host = ts.createCompilerHost(options)
  host.resolveModuleNameLiterals = (literals, containingFile) =>
    literals.map(({ text }) => {
      const resolved = ts.resolveModuleName(text, containingFile, options, host)
      const file = resolved.resolvedModule?.resolvedFileName
      return file != null && packages.has(packageNameOf(file))
        ? resolved
        : { resolvedModule: undefined }
    })
  const program = ts.createProgram({
    rootNames: [...globalEntries.map(({ entry }) => entry), ...systemEntries],
    options: { ...options, lib: manifest.libs.map(libFileName) },
    host,
  })

  program.getSourceFiles().forEach((sourceFile) => {
    const { fileName, text } = sourceFile
    if (program.isSourceFileDefaultLibrary(sourceFile)) {
      files.set(`/lib/${basename(fileName)}`, compactTypings(text, true))
      return
    }
    const pkg = packages.get(packageNameOf(fileName))
    if (pkg == null) return
    putFirst(virtualPath(fileName), () =>
      compactTypings(text, pkg.docs ?? true)
    )
    putFirst(`${NODE_MODULES}${pkg.name}/package.json`, () => {
      const directory = fileName.slice(
        0,
        fileName.lastIndexOf(NODE_MODULES) +
          NODE_MODULES.length +
          pkg.name.length
      )
      const { version, types, typings } = JSON.parse(
        readFileSync(join(directory, 'package.json'), 'utf-8')
      )
      return JSON.stringify({
        name: pkg.name,
        version,
        types: types ?? typings ?? 'index.d.ts',
      })
    })
  })

  manifest.packages
    .filter(({ name }) => !files.has(`${NODE_MODULES}${name}/package.json`))
    .forEach(({ name }) => {
      throw new Error(`Package '${name}' isn't installed, or nothing uses it.`)
    })

  return {
    libs: manifest.libs.map(libFileName),
    roots: globalEntries.map(({ root }) => root),
    files: Object.fromEntries(files),
  }
}

function libFileName(name: string) {
  return `lib.${name.toLowerCase()}.d.ts`
}

function normalize(file: string) {
  return file.replace(/\\/g, '/')
}

function virtualPath(file: string) {
  return normalize(file).slice(normalize(file).lastIndexOf(NODE_MODULES))
}

function packageNameOf(file: string) {
  const [first = '', second] = virtualPath(file)
    .slice(NODE_MODULES.length)
    .split('/')
  if (!normalize(file).includes(NODE_MODULES)) return ''
  return first.startsWith('@') ? `${first}/${second}` : first
}
