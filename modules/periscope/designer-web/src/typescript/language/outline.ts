import ts from 'typescript'
import { OutlineItem } from './protocol'

const TYPE_KINDS: string[] = [
  ts.ScriptElementKind.classElement,
  ts.ScriptElementKind.localClassElement,
  ts.ScriptElementKind.interfaceElement,
  ts.ScriptElementKind.enumElement,
]

const MEMBER_KINDS: string[] = [
  ts.ScriptElementKind.memberVariableElement,
  ts.ScriptElementKind.memberFunctionElement,
  ts.ScriptElementKind.memberGetAccessorElement,
  ts.ScriptElementKind.memberSetAccessorElement,
]

export function outlineOf(
  parent: ts.NavigationTree,
  sourceFile: ts.SourceFile
): OutlineItem[] {
  const isType = TYPE_KINDS.includes(parent.kind)
  return (parent.childItems ?? [])
    .filter(isDeclaration)
    .filter((item) => isType || !MEMBER_KINDS.includes(item.kind))
    .map((item) => toOutlineItem(item, sourceFile))
}

function toOutlineItem(
  item: ts.NavigationTree,
  sourceFile: ts.SourceFile
): OutlineItem {
  return {
    text: item.text,
    detail: parametersOf(item, sourceFile),
    kind: item.kind,
    span: item.nameSpan ?? item.spans[0],
    children: outlineOf(item, sourceFile),
  }
}

function isDeclaration(item: ts.NavigationTree) {
  const callback =
    item.text === '<function>' || item.text.endsWith(') callback')
  return item.kind !== 'alias' && !callback
}

function parametersOf(item: ts.NavigationTree, sourceFile: ts.SourceFile) {
  const declaration = functionAt(sourceFile, item.spans[0])
  if (declaration == null) return ''
  const names = declaration.parameters.map(
    (parameter) =>
      (parameter.dotDotDotToken ? '...' : '') +
      parameter.name.getText(sourceFile)
  )
  return `(${names.join(', ')})`
}

function functionAt(sourceFile: ts.SourceFile, span: ts.TextSpan) {
  const end = span.start + span.length
  let found: ts.SignatureDeclaration | undefined
  const visit = (node: ts.Node) => {
    if (found || node.pos > span.start || node.end < end) return
    if (node.getStart(sourceFile) === span.start) found = functionOf(node)
    if (found == null) ts.forEachChild(node, visit)
  }
  visit(sourceFile)
  return found
}

function functionOf(node: ts.Node): ts.SignatureDeclaration | undefined {
  if (ts.isFunctionLike(node)) return node
  const initializer = ts.isVariableDeclaration(node) ? node.initializer : null
  return initializer && ts.isFunctionLike(initializer) ? initializer : undefined
}
