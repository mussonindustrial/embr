import ts from 'typescript'
import { GLOBALS } from './globals'

const BROWSER_PREFIXES = [
  './',
  '../',
  '@/',
  'system:',
  '@webdev/',
  'http://',
  'https://',
]

const UNTYPED_PREFIXES = ['@webdev/', 'http://', 'https://']

const MODULE_NOT_FOUND = [2307, 2792]

// Outside TypeScript's range, so it can't clash with a real error.
export const UNAVAILABLE_IMPORT = 99001

// Imports the browser can't load replace TypeScript's "cannot find module" errors on them.
export function importDiagnostics(
  sourceFile: ts.SourceFile,
  semantic: ts.Diagnostic[]
): ts.Diagnostic[] {
  const unavailable = unavailableImports(sourceFile)
  const isExpected = (diagnostic: ts.Diagnostic) =>
    MODULE_NOT_FOUND.includes(diagnostic.code) &&
    (isUntyped(sourceFile, diagnostic) || covers(unavailable, diagnostic))
  return [...unavailable, ...semantic.filter((d) => !isExpected(d))]
}

function isUntyped(sourceFile: ts.SourceFile, diagnostic: ts.Diagnostic) {
  const start = (diagnostic.start ?? 0) + 1
  const specifier = sourceFile.text.slice(
    start,
    start + (diagnostic.length ?? 2) - 2
  )
  return UNTYPED_PREFIXES.some((prefix) => specifier.startsWith(prefix))
}

function covers(diagnostics: ts.Diagnostic[], diagnostic: ts.Diagnostic) {
  const start = diagnostic.start ?? -1
  return diagnostics.some(
    ({ start: from = 0, length = 0 }) => start >= from && start < from + length
  )
}

function unavailableImports(sourceFile: ts.SourceFile): ts.Diagnostic[] {
  return importSpecifiers(sourceFile)
    .filter((specifier) => !isAvailable(specifier.text))
    .map((specifier) => ({
      category: ts.DiagnosticCategory.Error,
      code: UNAVAILABLE_IMPORT,
      file: sourceFile,
      start: specifier.getStart(sourceFile),
      length: specifier.getWidth(sourceFile),
      messageText: messageFor(specifier.text),
    }))
}

function isAvailable(specifier: string) {
  return BROWSER_PREFIXES.some((prefix) => specifier.startsWith(prefix))
}

function messageFor(specifier: string) {
  const global = GLOBALS[specifier]
  if (global != null) {
    return `'${specifier}' can't be imported in the browser: use the global '${global}', or 'import type' for its types.`
  }
  return `'${specifier}' isn't available in the browser. Import client resources ('@/'), system modules ('system:'), WebDev resources ('@webdev/') or URLs.`
}

function importSpecifiers(sourceFile: ts.SourceFile) {
  const specifiers: ts.StringLiteral[] = []
  const visit = (node: ts.Node) => {
    const specifier = valueImportSpecifier(node)
    if (specifier != null) specifiers.push(specifier)
    ts.forEachChild(node, visit)
  }
  visit(sourceFile)
  return specifiers
}

function valueImportSpecifier(node: ts.Node): ts.StringLiteral | undefined {
  if (ts.isImportDeclaration(node) && !isTypeOnly(node.importClause)) {
    return asString(node.moduleSpecifier)
  }
  if (ts.isExportDeclaration(node) && !node.isTypeOnly) {
    return asString(node.moduleSpecifier)
  }
  if (
    ts.isCallExpression(node) &&
    node.expression.kind === ts.SyntaxKind.ImportKeyword
  ) {
    return asString(node.arguments[0])
  }
  return undefined
}

function isTypeOnly(clause: ts.ImportClause | undefined) {
  if (clause == null) return false
  if (clause.isTypeOnly) return true
  const bindings = clause.namedBindings
  return (
    clause.name == null &&
    bindings != null &&
    ts.isNamedImports(bindings) &&
    bindings.elements.length > 0 &&
    bindings.elements.every((element) => element.isTypeOnly)
  )
}

function asString(node: ts.Node | undefined) {
  return node != null && ts.isStringLiteral(node) ? node : undefined
}
