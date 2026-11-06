import ts from 'typescript'
import { RESOURCE_ROOT } from './files'

export function importCompletions(
  sourceFile: ts.SourceFile,
  offset: number,
  entries: readonly ts.CompletionEntry[],
  systemModules: string[]
): ts.CompletionEntry[] {
  const path = nodesAt(sourceFile, offset)
  const specifier = path.find(isModuleSpecifier)
  if (specifier != null) {
    return specifierEntries(
      sourceFile,
      specifier,
      offset,
      entries,
      systemModules
    )
  }
  if (path.some(ts.isNamedImports)) {
    return entries.filter(
      (entry) => entry.kind !== ts.ScriptElementKind.keyword
    )
  }
  return [...entries]
}

function specifierEntries(
  sourceFile: ts.SourceFile,
  specifier: ts.StringLiteral,
  offset: number,
  entries: readonly ts.CompletionEntry[],
  systemModules: string[]
) {
  const start = specifier.getStart() + 1
  const typed = sourceFile.text.slice(start, offset)
  const directory = typed.slice(0, typed.lastIndexOf('/') + 1)
  const resolved = resolvedDirectory(sourceFile.fileName, directory)
  const quote = sourceFile.text[specifier.getStart()]
  const closing = isClosed(specifier, quote) ? '' : quote
  const relative = relativeEntries(sourceFile.fileName, directory, resolved)

  const importable =
    resolved === ''
      ? bareEntries(systemModules)
      : entries.filter(isResourceIn(resolved))

  return [...relative, ...importable].map((entry) => {
    const isModule = entry.kind === ts.ScriptElementKind.externalModuleName
    const spanStart = isModule ? start : start + directory.length
    const isDirectory = entry.kind === ts.ScriptElementKind.directory
    return {
      ...entry,
      insertText: entry.name + (isDirectory ? '' : closing),
      replacementSpan: { start: spanStart, length: offset - spanStart },
    }
  })
}

function relativeEntries(
  fileName: string,
  directory: string,
  resolved: string | null
): ts.CompletionEntry[] {
  const isRelative = directory === '' || directory.startsWith('.')
  if (!isRelative) return []

  const names = directory === '' ? ['.', '..'] : ['..']
  const top = directory === '' ? resolvedDirectory(fileName, './') : resolved
  return names
    .filter((name) => name === '.' || top !== RESOURCE_ROOT)
    .map((name) => entry(name, ts.ScriptElementKind.directory))
}

// TypeScript names `system:` modules differently before and after the colon.
function bareEntries(systemModules: string[]): ts.CompletionEntry[] {
  return [
    entry('@', ts.ScriptElementKind.directory),
    ...systemModules.map((name) =>
      entry(`system:${name}`, ts.ScriptElementKind.externalModuleName)
    ),
  ]
}

function isResourceIn(directory: string | null) {
  const isResource = directory?.startsWith(RESOURCE_ROOT) ?? false
  return (entry: ts.CompletionEntry) =>
    isResource && !entry.name.startsWith('system:')
}

function entry(name: string, kind: ts.ScriptElementKind): ts.CompletionEntry {
  return { name, kind, kindModifiers: '', sortText: '0' }
}

// '' for a bare specifier, null for a package's path, which the browser can't import.
function resolvedDirectory(fileName: string, directory: string) {
  if (directory === '') return ''
  if (directory.startsWith('@/')) return RESOURCE_ROOT + directory.slice(2)
  if (!directory.startsWith('./') && !directory.startsWith('../')) return null

  const segments = fileName.split('/').slice(1, -1)
  directory
    .split('/')
    .filter((segment) => segment !== '' && segment !== '.')
    .forEach((segment) =>
      segment === '..' ? segments.pop() : segments.push(segment)
    )
  return '/' + segments.map((segment) => segment + '/').join('')
}

function isClosed(specifier: ts.StringLiteral, quote: string) {
  const text = specifier.getText()
  return text.length > 1 && text.endsWith(quote)
}

function isModuleSpecifier(node: ts.Node): node is ts.StringLiteral {
  if (!ts.isStringLiteral(node)) return false
  const parent = node.parent
  if (ts.isImportDeclaration(parent) || ts.isExportDeclaration(parent)) {
    return parent.moduleSpecifier === node
  }
  return (
    ts.isCallExpression(parent) &&
    parent.expression.kind === ts.SyntaxKind.ImportKeyword
  )
}

// Innermost first.
function nodesAt(sourceFile: ts.SourceFile, offset: number) {
  const nodes: ts.Node[] = []
  const visit = (node: ts.Node) => {
    if (offset < node.getStart(sourceFile) || offset > node.end) return
    nodes.unshift(node)
    ts.forEachChild(node, visit)
  }
  ts.forEachChild(sourceFile, visit)
  return nodes
}
