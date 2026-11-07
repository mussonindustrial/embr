import { existsSync, readFileSync } from 'fs'
import { createRequire } from 'module'
import { dirname, join, resolve } from 'path'
import ts from 'typescript'
import type { Typings } from '../../src/language/protocol'
import { compactTypings } from './compactTypings'
import { SystemModuleFile, systemModuleFiles } from './systemModules'

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

const resolutionOptions: ts.CompilerOptions = {
  module: ts.ModuleKind.ESNext,
  moduleResolution: ts.ModuleResolutionKind.Bundler,
}

// Throws on anything missing, so a moved or renamed package fails the build.
export function collectTypings(
  manifest: TypingsManifest,
  projectRoot: string
): Typings {
  const collector = new TypingsCollector(manifest, projectRoot)
  manifest.libs.forEach((lib) => collector.addLib(lib))
  manifest.packages.forEach((pkg) => collector.addPackage(pkg.name))
  const roots = Object.entries(manifest.globals).map(([pkg, global]) =>
    collector.addGlobal(pkg, global)
  )
  systemModuleFiles(manifest.systemModules, projectRoot).forEach((file) =>
    collector.addSystemFile(file)
  )
  return {
    libs: manifest.libs.map(libFileName),
    roots,
    files: Object.fromEntries(collector.files),
  }
}

class TypingsCollector {
  readonly files = new Map<string, string>()
  private readonly sources = new Map<string, string>()
  private readonly libs = new Set<string>()
  private readonly added = new Set<string>()
  private readonly packages: Map<string, TypingsPackage>
  private readonly projectFile: string
  private readonly libDirectory: string

  constructor(manifest: TypingsManifest, projectRoot: string) {
    this.packages = new Map(manifest.packages.map((pkg) => [pkg.name, pkg]))
    this.projectFile = join(projectRoot, 'package.json')
    this.libDirectory = dirname(
      createRequire(this.projectFile).resolve('typescript')
    )
  }

  addLib(name: string) {
    const fileName = libFileName(name)
    if (this.libs.has(fileName)) return
    this.libs.add(fileName)

    const text = readRequired(join(this.libDirectory, fileName))
    this.put(`/lib/${fileName}`, compactTypings(text, true), fileName)
    ts.preProcessFile(text).libReferenceDirectives.forEach((lib) =>
      this.addLib(lib.fileName)
    )
  }

  addPackage(name: string, from = this.projectFile) {
    if (this.added.has(name)) return
    this.added.add(name)
    const directory = this.packageDirectory(name, from)
    const manifest = JSON.parse(readRequired(join(directory, 'package.json')))
    const entry = manifest.types ?? manifest.typings ?? 'index.d.ts'
    const packageJson = { name, version: manifest.version, types: entry }
    this.put(
      virtualPath(join(directory, 'package.json')),
      JSON.stringify(packageJson),
      directory
    )
    this.addPackageFile(
      join(directory, entry),
      this.packages.get(name) ?? { name }
    )
  }

  // The root file declaring the global: the package's own, or one written for it.
  addGlobal(pkg: string, global: string) {
    this.addListed(pkg, this.projectFile, unlisted)
    const entry = this.resolveModule(pkg, this.projectFile)
    if (readRequired(entry).includes(`export as namespace ${global}`)) {
      return virtualPath(entry)
    }
    const path = `/globals/${global}.d.ts`
    this.put(
      path,
      `export * from '${pkg}'\nexport as namespace ${global}\n`,
      pkg
    )
    return path
  }

  addSystemFile({ path, text, source }: SystemModuleFile) {
    this.put(path, compactTypings(text, true), source)
    this.followPackages(source, text, (specifier) =>
      this.addDependency(specifier, source)
    )
  }

  // The system module's own package depends on it, so the manifest needn't list it.
  private addDependency(specifier: string, file: string) {
    const name = packageName(specifier)
    this.packages.set(name, { name })
    this.addListed(specifier, file, unlisted)
  }

  private addPackageFile(file: string, pkg: TypingsPackage) {
    const path = virtualPath(file)
    if (this.files.has(path)) return

    const text = readRequired(file)
    this.put(path, compactTypings(text, pkg.docs ?? true), file)
    this.followRelative(file, text)
    this.followPackages(file, text, () => {})
  }

  private followRelative(file: string, text: string) {
    const { referencedFiles, importedFiles } = ts.preProcessFile(
      text,
      true,
      true
    )
    referencedFiles.forEach((reference) =>
      this.addResolved(resolve(dirname(file), reference.fileName))
    )
    importedFiles
      .filter(({ fileName }) => isRelative(fileName) && !isAsset(fileName))
      .forEach(({ fileName }) =>
        this.addResolved(this.resolveModule(fileName, file))
      )
  }

  private followPackages(
    file: string,
    text: string,
    onUnlisted: (specifier: string, file: string) => void
  ) {
    const { importedFiles, typeReferenceDirectives } = ts.preProcessFile(
      text,
      true,
      true
    )
    ;[...importedFiles, ...typeReferenceDirectives]
      .filter(({ fileName }) => !isRelative(fileName) && !isAsset(fileName))
      .forEach(({ fileName }) => this.addListed(fileName, file, onUnlisted))
  }

  private addListed(
    specifier: string,
    file: string,
    onUnlisted: (specifier: string, file: string) => void
  ) {
    const listed = this.listed(specifier)
    if (listed == null) {
      onUnlisted(specifier, file)
      return
    }
    this.addPackage(listed, file)
    this.addResolved(this.resolveModule(specifier, file))
  }

  // An import's package, or its `@types` package.
  private listed(specifier: string) {
    const name = packageName(specifier)
    const typesName = `@types/${name.replace('@', '').replace('/', '__')}`
    return [name, typesName].find((candidate) => this.packages.has(candidate))
  }

  private addResolved(file: string) {
    const owner = this.packages.get(packageNameOf(file))
    this.addPackageFile(file, owner ?? { name: packageNameOf(file) })
  }

  private resolveModule(specifier: string, containingFile: string) {
    const { resolvedModule } = ts.resolveModuleName(
      specifier,
      containingFile,
      resolutionOptions,
      ts.sys
    )
    if (resolvedModule == null) {
      throw new Error(`Can't resolve '${specifier}' from '${containingFile}'.`)
    }
    return resolvedModule.resolvedFileName
  }

  private packageDirectory(name: string, from: string) {
    const searched = createRequire(from).resolve.paths(name) ?? []
    const directory = searched
      .map((nodeModules) => join(nodeModules, name))
      .find((candidate) => existsSync(join(candidate, 'package.json')))
    if (directory == null) {
      throw new Error(`Package '${name}' isn't installed.`)
    }
    return directory
  }

  private put(path: string, text: string, source: string) {
    const existing = this.sources.get(path)
    if (existing != null && existing !== source) {
      throw new Error(
        `'${path}' comes from both '${existing}' and '${source}'.`
      )
    }
    this.sources.set(path, source)
    this.files.set(path, text)
  }
}

function unlisted(specifier: string, file: string): never {
  throw new Error(
    `Package '${specifier}' in '${file}' isn't in the typings manifest.`
  )
}

function libFileName(name: string) {
  return `lib.${name.toLowerCase()}.d.ts`
}

function readRequired(file: string) {
  if (!existsSync(file)) throw new Error(`Typings file '${file}' not found.`)
  return readFileSync(file, 'utf-8')
}

const SCRIPT_EXTENSIONS = [
  '.js',
  '.mjs',
  '.cjs',
  '.ts',
  '.mts',
  '.cts',
  '.jsx',
  '.tsx',
]

// A stylesheet, image or other file without declarations, such as `./main.scss`.
function isAsset(specifier: string) {
  const extension = /\.[a-z0-9]+$/i.exec(specifier)?.[0]
  return extension != null && !SCRIPT_EXTENSIONS.includes(extension)
}

function isRelative(specifier: string) {
  return specifier.startsWith('./') || specifier.startsWith('../')
}

// `react`, or `@scope/name` from `@scope/name/sub`.
function packageName(specifier: string) {
  const parts = specifier.split('/')
  return specifier.startsWith('@') ? parts.slice(0, 2).join('/') : parts[0]
}

// Everything from the file's innermost `node_modules`.
function virtualPath(file: string) {
  const normalized = file.replace(/\\/g, '/')
  const index = normalized.lastIndexOf(NODE_MODULES)
  if (index < 0) throw new Error(`'${file}' isn't inside a package.`)
  return normalized.slice(index)
}

function packageNameOf(file: string) {
  return packageName(virtualPath(file).slice(NODE_MODULES.length))
}
