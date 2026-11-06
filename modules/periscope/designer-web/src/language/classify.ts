import ts from 'typescript'
import { TokenKind } from './protocol'

const Syntactic = ts.ClassificationType

const syntacticKinds: Partial<Record<ts.ClassificationType, TokenKind>> = {
  [Syntactic.identifier]: TokenKind.TEXT,
  [Syntactic.keyword]: TokenKind.KEYWORD,
  [Syntactic.numericLiteral]: TokenKind.NUMBER,
  [Syntactic.bigintLiteral]: TokenKind.NUMBER,
  [Syntactic.operator]: TokenKind.OPERATOR,
  [Syntactic.stringLiteral]: TokenKind.STRING,
  [Syntactic.jsxAttributeStringLiteralValue]: TokenKind.STRING,
  [Syntactic.regularExpressionLiteral]: TokenKind.REGEX,
  [Syntactic.className]: TokenKind.TYPE,
  [Syntactic.enumName]: TokenKind.TYPE,
  [Syntactic.interfaceName]: TokenKind.TYPE,
  [Syntactic.moduleName]: TokenKind.TYPE,
  [Syntactic.typeParameterName]: TokenKind.TYPE,
  [Syntactic.typeAliasName]: TokenKind.TYPE,
  [Syntactic.parameterName]: TokenKind.PARAMETER,
  [Syntactic.docCommentTagName]: TokenKind.DOC_COMMENT_TAG,
  [Syntactic.jsxOpenTagName]: TokenKind.JSX_TAG,
  [Syntactic.jsxCloseTagName]: TokenKind.JSX_TAG,
  [Syntactic.jsxSelfClosingTagName]: TokenKind.JSX_TAG,
  [Syntactic.jsxAttribute]: TokenKind.JSX_ATTRIBUTE,
  [Syntactic.jsxText]: TokenKind.JSX_TEXT,
}

// Must stay in TypeScript's `TokenType` order.
const semanticKinds: TokenKind[] = [
  TokenKind.TYPE,
  TokenKind.TYPE,
  TokenKind.TYPE,
  TokenKind.TYPE,
  TokenKind.TYPE,
  TokenKind.TYPE,
  TokenKind.PARAMETER,
  TokenKind.VARIABLE,
  TokenKind.CONSTANT,
  TokenKind.PROPERTY,
  TokenKind.FUNCTION,
  TokenKind.FUNCTION,
]

const VARIABLE_TYPE = 7
const READONLY_MODIFIER = 1 << 3
const TYPE_OFFSET = 8
const MODIFIER_MASK = (1 << TYPE_OFFSET) - 1

function semanticKind(encoded: number) {
  const type = (encoded >> TYPE_OFFSET) - 1
  const readonly = (encoded & MODIFIER_MASK & READONLY_MODIFIER) !== 0
  if (type === VARIABLE_TYPE && readonly) {
    return TokenKind.CONSTANT
  }
  return semanticKinds[type]
}

function punctuationKind(text: string) {
  return '(){}[]'.includes(text) ? TokenKind.BRACKET : TokenKind.PUNCTUATION
}

function commentKind(text: string, start: number) {
  if (text.startsWith('//', start)) return TokenKind.LINE_COMMENT
  if (text.startsWith('/**', start)) return TokenKind.DOC_COMMENT
  return TokenKind.BLOCK_COMMENT
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

  const tokens: number[] = []
  let docCommentEnd = -1

  for (let i = 0; i < spans.length; i += 3) {
    const [start, length, type] = [spans[i], spans[i + 1], spans[i + 2]]
    if (type === Syntactic.whiteSpace) continue

    let kind: TokenKind
    if (start < docCommentEnd) {
      kind =
        type === Syntactic.docCommentTagName
          ? TokenKind.DOC_COMMENT_TAG
          : TokenKind.DOC_COMMENT
    } else if (type === Syntactic.comment) {
      kind = commentKind(text, start)
      if (kind === TokenKind.DOC_COMMENT) {
        const end = text.indexOf('*/', start + 3)
        docCommentEnd = end < 0 ? text.length : end + 2
      }
    } else if (type === Syntactic.punctuation) {
      kind = punctuationKind(text.slice(start, start + length))
    } else {
      kind =
        semantic.get(start) ??
        syntacticKinds[type as ts.ClassificationType] ??
        TokenKind.TEXT
    }
    tokens.push(start, length, kind)
  }

  return tokens
}
