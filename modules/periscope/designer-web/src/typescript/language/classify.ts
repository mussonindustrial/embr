import ts from 'typescript'
import { TokenKind } from './protocol'

const Syntactic = ts.ClassificationType

const syntacticKinds: Partial<Record<ts.ClassificationType, TokenKind>> = {
  [Syntactic.identifier]: 'TEXT',
  [Syntactic.keyword]: 'KEYWORD',
  [Syntactic.numericLiteral]: 'NUMBER',
  [Syntactic.bigintLiteral]: 'NUMBER',
  [Syntactic.operator]: 'OPERATOR',
  [Syntactic.stringLiteral]: 'STRING',
  [Syntactic.jsxAttributeStringLiteralValue]: 'STRING',
  [Syntactic.regularExpressionLiteral]: 'REGEX',
  [Syntactic.className]: 'TYPE',
  [Syntactic.enumName]: 'TYPE',
  [Syntactic.interfaceName]: 'TYPE',
  [Syntactic.moduleName]: 'TYPE',
  [Syntactic.typeParameterName]: 'TYPE',
  [Syntactic.typeAliasName]: 'TYPE',
  [Syntactic.parameterName]: 'PARAMETER',
  [Syntactic.docCommentTagName]: 'DOC_COMMENT_TAG',
  [Syntactic.jsxOpenTagName]: 'JSX_TAG',
  [Syntactic.jsxCloseTagName]: 'JSX_TAG',
  [Syntactic.jsxSelfClosingTagName]: 'JSX_TAG',
  [Syntactic.jsxAttribute]: 'JSX_ATTRIBUTE',
  [Syntactic.jsxText]: 'JSX_TEXT',
}

const SemanticType = {
  class: 0,
  enum: 1,
  interface: 2,
  namespace: 3,
  typeParameter: 4,
  type: 5,
  parameter: 6,
  variable: 7,
  enumMember: 8,
  property: 9,
  function: 10,
  member: 11,
}
const READONLY_MODIFIER = 1 << 3
const TYPE_OFFSET = 8
const MODIFIER_MASK = (1 << TYPE_OFFSET) - 1

const semanticKinds: Partial<Record<number, TokenKind>> = {
  [SemanticType.class]: 'TYPE',
  [SemanticType.enum]: 'TYPE',
  [SemanticType.interface]: 'TYPE',
  [SemanticType.namespace]: 'TYPE',
  [SemanticType.typeParameter]: 'TYPE',
  [SemanticType.type]: 'TYPE',
  [SemanticType.parameter]: 'PARAMETER',
  [SemanticType.variable]: 'VARIABLE',
  [SemanticType.enumMember]: 'CONSTANT',
  [SemanticType.property]: 'PROPERTY',
  [SemanticType.function]: 'FUNCTION',
  [SemanticType.member]: 'FUNCTION',
}

function semanticKind(encoded: number): TokenKind | undefined {
  const type = (encoded >> TYPE_OFFSET) - 1
  const readonly = (encoded & MODIFIER_MASK & READONLY_MODIFIER) !== 0
  if (type === SemanticType.variable && readonly) return 'CONSTANT'
  return semanticKinds[type]
}

function punctuationKind(text: string) {
  return '(){}[]'.includes(text) ? 'BRACKET' : 'PUNCTUATION'
}

function commentKind(text: string, start: number) {
  if (text.startsWith('//', start)) return 'LINE_COMMENT'
  if (text.startsWith('/**', start)) return 'DOC_COMMENT'
  return 'BLOCK_COMMENT'
}

function semanticKindsByStart(
  service: ts.LanguageService,
  fileName: string,
  span: ts.TextSpan
) {
  const { spans } = service.getEncodedSemanticClassifications(
    fileName,
    span,
    ts.SemanticClassificationFormat.TwentyTwenty
  )
  const kinds = new Map<number, TokenKind>()
  for (let i = 0; i < spans.length; i += 3) {
    const kind = semanticKind(spans[i + 2])
    if (kind != null) kinds.set(spans[i], kind)
  }
  return kinds
}

export function classify(
  service: ts.LanguageService,
  fileName: string,
  text: string
) {
  const span = { start: 0, length: text.length }
  const { spans } = service.getEncodedSyntacticClassifications(fileName, span)
  const semantic = semanticKindsByStart(service, fileName, span)

  const kinds: TokenKind[] = []
  const tokens: number[] = []
  const indexOf = (kind: TokenKind) =>
    kinds.includes(kind) ? kinds.indexOf(kind) : kinds.push(kind) - 1
  let docCommentEnd = -1

  for (let i = 0; i < spans.length; i += 3) {
    const [start, length, type] = [spans[i], spans[i + 1], spans[i + 2]]
    if (type === Syntactic.whiteSpace) continue

    let kind: TokenKind
    if (start < docCommentEnd) {
      kind =
        type === Syntactic.docCommentTagName ? 'DOC_COMMENT_TAG' : 'DOC_COMMENT'
    } else if (type === Syntactic.comment) {
      kind = commentKind(text, start)
      if (kind === 'DOC_COMMENT') {
        const end = text.indexOf('*/', start + 3)
        docCommentEnd = end < 0 ? text.length : end + 2
      }
    } else if (type === Syntactic.punctuation) {
      kind = punctuationKind(text.slice(start, start + length))
    } else {
      kind =
        semantic.get(start) ??
        syntacticKinds[type as ts.ClassificationType] ??
        'TEXT'
    }
    tokens.push(start, length, indexOf(kind))
  }

  return { kinds, tokens }
}
