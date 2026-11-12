import { dirname, join, posix, relative } from 'path'
import ts from 'typescript'
import {
  SYSTEM_MODULES,
  TYPINGS_ROOT,
} from '../../src/typescript/language/files'

const DECLARATIONS = `${TYPINGS_ROOT}declarations/`

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
  outDir: DECLARATIONS,
}

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
    DECLARATIONS + normalize(relative(root, source)).replace(/\.tsx?$/, '.d.ts')

  const files = [...declarations].map(([source, text]) => ({
    path: pathOf(source),
    text: linkImports(text, source, declarations, pathOf),
    source,
  }))
  const moduleFiles = entries.map(({ name, source }) => ({
    path: `${SYSTEM_MODULES}${name}.d.ts`,
    text: moduleDeclaration(
      name,
      specifierOf(posix.relative(SYSTEM_MODULES, pathOf(source))),
      typeAliasesOf(declarations.get(source)!)
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

function moduleDeclaration(
  name: string,
  factorySpecifier: string,
  typeAliases: string[]
) {
  const factoryName = `create${name[0].toUpperCase()}${name.slice(1)}Api`
  return [
    `import type * as factory from '${factorySpecifier}'`,
    `declare const ${name}: ReturnType<typeof factory.${factoryName}>`,
    `declare namespace ${name} {`,
    ...typeAliases.map((alias) => `  export ${alias}`),
    '}',
    `export = ${name}`,
    '',
  ].join('\n')
}

function typeAliasesOf(declaration: string) {
  const isExported = (statement: ts.Statement) =>
    ts.canHaveModifiers(statement) &&
    ts
      .getModifiers(statement)
      ?.some((modifier) => modifier.kind === ts.SyntaxKind.ExportKeyword)
  const textOf = (node: ts.Node) => declaration.slice(node.pos, node.end).trim()

  return ts
    .createSourceFile('declaration.d.ts', declaration, ts.ScriptTarget.Latest)
    .statements.filter(isExported)
    .filter(
      (statement) =>
        ts.isTypeAliasDeclaration(statement) ||
        ts.isInterfaceDeclaration(statement)
    )
    .map(({ name, typeParameters = [] }) => {
      const parameters = typeParameters.map(textOf)
      const typeArguments = typeParameters.map((parameter) =>
        textOf(parameter.name)
      )
      const generic = (items: string[]) =>
        items.length > 0 ? `<${items.join(', ')}>` : ''
      return `type ${name.text}${generic(parameters)} = factory.${name.text}${generic(typeArguments)}`
    })
}

function commonDirectory(files: string[]) {
  return files.map(dirname).reduce(
    (common, directory) => {
      while (!`${directory}/`.startsWith(`${common}/`)) common = dirname(common)
      return common
    },
    dirname(files[0] ?? '')
  )
}

function normalize(file: string) {
  return file.replace(/\\/g, '/')
}
