import { dirname, join, posix, relative } from 'path'
import ts from 'typescript'

export type SystemModuleFile = {
  path: string
  text: string
  source: string
}

const compilerOptions: ts.CompilerOptions = {
  target: ts.ScriptTarget.ES2020,
  module: ts.ModuleKind.ESNext,
  moduleResolution: ts.ModuleResolutionKind.Bundler,
  jsx: ts.JsxEmit.React,
  strict: true,
  esModuleInterop: true,
  skipLibCheck: true,
  declaration: true,
  emitDeclarationOnly: true,
  outDir: '/declarations',
}

// Typed from the `create<Name>Module` factory the client builds each module with, so they can't drift.
// Only the modules go in `/system/`, so import completions list nothing else there.
export function systemModuleFiles(
  modules: Record<string, string>,
  projectRoot: string
): SystemModuleFile[] {
  const projectFile = join(projectRoot, 'package.json')
  const entries = Object.entries(modules).map(([name, specifier]) => ({
    name,
    source: resolveEntry(specifier, projectFile),
  }))
  const declarations = declarationsFrom(entries.map((entry) => entry.source))
  const root = commonDirectory([...declarations.keys()])
  const pathOf = (source: string) =>
    '/declarations/' +
    normalize(relative(root, source)).replace(/\.tsx?$/, '.d.ts')

  const files = [...declarations].map(([source, text]) => ({
    path: pathOf(source),
    text: linkImports(text, source, declarations, pathOf),
    source,
  }))
  const moduleFiles = entries.map(({ name, source }) => ({
    path: `/system/${name}.d.ts`,
    text: moduleDeclaration(
      name,
      source,
      specifierOf(posix.relative('/system', pathOf(source)))
    ),
    source,
  }))
  return [...files, ...moduleFiles]
}

function resolveEntry(specifier: string, projectFile: string) {
  const entry = resolveSource(specifier, projectFile)
  if (entry == null) {
    throw new Error(`Can't resolve system module '${specifier}'.`)
  }
  return entry
}

// A source another program didn't emit gets its own, as TypeScript emits nothing for another
// workspace package's sources.
function declarationsFrom(entries: string[]) {
  const emitted = new Map<string, string>()
  const declarations = new Map<string, string>()
  const visit = (source: string) => {
    if (declarations.has(source) || source.endsWith('.d.ts')) return
    if (!emitted.has(source)) emitDeclarations(source, emitted)
    const text = emitted.get(source)!
    declarations.set(source, text)
    ts.preProcessFile(text, true, true)
      .importedFiles.map(({ fileName }) => resolveSource(fileName, source))
      .forEach((resolved) => resolved && visit(resolved))
  }
  entries.forEach(visit)
  return declarations
}

function emitDeclarations(source: string, into: Map<string, string>) {
  ts.createProgram([source], compilerOptions).emit(
    undefined,
    (_, text, __, ___, sourceFiles) => {
      const file = sourceFiles?.[0]
      if (file != null) into.set(normalize(file.fileName), text)
    }
  )
}

// Points imports of other declared sources, packages included, at their declarations.
function linkImports(
  text: string,
  source: string,
  declarations: Map<string, string>,
  pathOf: (source: string) => string
) {
  const directory = posix.dirname(pathOf(source))
  return ts
    .preProcessFile(text, true, true)
    .importedFiles.reverse()
    .reduce((linked, { fileName, pos }) => {
      const target = resolveSource(fileName, source)
      if (target == null || !declarations.has(target)) return linked
      const start = linked.indexOf(fileName, pos)
      return (
        linked.slice(0, start) +
        specifierOf(posix.relative(directory, pathOf(target))) +
        linked.slice(start + fileName.length)
      )
    }, text)
}

function specifierOf(path: string) {
  return (path.startsWith('.') ? path : `./${path}`).replace(/\.d\.ts$/, '')
}

function resolveSource(specifier: string, containingFile: string) {
  const { resolvedModule } = ts.resolveModuleName(
    specifier,
    containingFile,
    compilerOptions,
    ts.sys
  )
  return resolvedModule && normalize(resolvedModule.resolvedFileName)
}

// Re-exports the factory's properties as the module's named exports, with their documentation.
function moduleDeclaration(
  name: string,
  source: string,
  factorySpecifier: string
) {
  const program = ts.createProgram([source], compilerOptions)
  const checker = program.getTypeChecker()
  const factoryName = `create${name[0].toUpperCase()}${name.slice(1)}Module`
  const factory = factoryOf(program, source, factoryName)
  const signature = checker.getSignaturesOfType(
    checker.getTypeOfSymbol(factory),
    ts.SignatureKind.Call
  )[0]
  const exports = checker
    .getReturnTypeOfSignature(signature)
    .getProperties()
    .map((property) => {
      const docs = ts.displayPartsToString(
        property.getDocumentationComment(checker)
      )
      const comment = docs ? `/** ${docs} */\n` : ''
      return `${comment}export declare const ${property.name}: typeof ${name}.${property.name}\n`
    })

  return [
    `import type { ${factoryName} } from '${factorySpecifier}'\n`,
    `declare const ${name}: ReturnType<typeof ${factoryName}>\n`,
    `export default ${name}\n`,
    ...exports,
  ].join('')
}

function factoryOf(program: ts.Program, source: string, factoryName: string) {
  const checker = program.getTypeChecker()
  const file = program
    .getSourceFiles()
    .find((f) => normalize(f.fileName) === source)
  const module = file && checker.getSymbolAtLocation(file)
  const factory =
    module &&
    checker.getExportsOfModule(module).find((s) => s.name === factoryName)
  if (factory == null) {
    throw new Error(`'${source}' doesn't export '${factoryName}'.`)
  }
  return factory
}

function commonDirectory(files: string[]) {
  return files.map(dirname).reduce((common, directory) => {
    while (!`${directory}/`.startsWith(`${common}/`)) common = dirname(common)
    return common
  })
}

function normalize(file: string) {
  return file.replace(/\\/g, '/')
}
