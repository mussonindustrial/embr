import { Diagnostic } from '../compiler/protocol'

export type ResourceKind = 'typescript' | 'javascript' | 'css'

export type ResourceFile = {
  path: string
  kind: ResourceKind
  source: string
}

export type SetFilesRequest = {
  files: ResourceFile[]
}

export type FileRequest = {
  path: string
}

export type PositionRequest = FileRequest & {
  offset: number
}

export type FormatOnTypeRequest = PositionRequest & {
  key: string
}

export type Span = {
  start: number
  length: number
}

// `path` is null for a library declaration.
export type Location = Span & {
  path: string | null
  line: number
  column: number
  lineText: string
}

export type TextEdit = Span & {
  newText: string
}

export type FileEdits = {
  path: string
  edits: TextEdit[]
}

export type CodeAction = {
  description: string
  changes: FileEdits[]
}

// Must stay in the order of the Designer's `TokenKind` enum.
export const TokenKind = {
  TEXT: 0,
  KEYWORD: 1,
  LINE_COMMENT: 2,
  BLOCK_COMMENT: 3,
  DOC_COMMENT: 4,
  DOC_COMMENT_TAG: 5,
  STRING: 6,
  NUMBER: 7,
  REGEX: 8,
  OPERATOR: 9,
  BRACKET: 10,
  PUNCTUATION: 11,
  TYPE: 12,
  FUNCTION: 13,
  PARAMETER: 14,
  VARIABLE: 15,
  CONSTANT: 16,
  PROPERTY: 17,
  JSX_TAG: 18,
  JSX_ATTRIBUTE: 19,
  JSX_TEXT: 20,
} as const

export type TokenKind = (typeof TokenKind)[keyof typeof TokenKind]

// Flat `start, length, kind` triples.
export type ClassificationsResult = {
  tokens: number[]
}

export type DiagnosticsResult = {
  diagnostics: Diagnostic[]
}

export type CompletionEntry = {
  name: string
  kind: string
  kindModifiers: string
  sortText: string
  insertText?: string
  replacementSpan?: Span
  source?: string
  data?: unknown
  hasAction: boolean
  isSnippet: boolean
}

export type CompletionsResult = {
  entries: CompletionEntry[]
  isMemberCompletion: boolean
}

export type CompletionDetailsRequest = PositionRequest & {
  name: string
  source?: string
  data?: unknown
}

export type CompletionDetailsResult = {
  display: string
  documentationHtml: string
  codeActions: CodeAction[]
}

export type QuickInfoResult = {
  span: Span
  display: string
  documentationHtml: string
}

export type DefinitionResult = {
  span: Span
  definitions: Location[]
}

export type ReferencesResult = {
  references: Location[]
}

export type HighlightsResult = {
  highlights: (Span & { write: boolean })[]
}

export type SignatureItem = {
  prefix: string
  parameters: string[]
  separator: string
  suffix: string
  documentationHtml: string
}

export type SignatureHelpResult = {
  items: SignatureItem[]
  selectedItem: number
  argumentIndex: number
}

export type CodeFixesRequest = FileRequest & {
  start: number
  end: number
}

export type CodeFixesResult = {
  actions: CodeAction[]
}

export type EditsResult = {
  edits: TextEdit[]
}

export type JsxClosingTagResult = {
  newText: string
}

export type OutlineItem = {
  text: string
  detail: string
  kind: string
  span: Span
  children: OutlineItem[]
}

export type OutlineResult = {
  items: OutlineItem[]
}

export type SymbolsRequest = {
  query: string
}

export type SymbolItem = {
  name: string
  kind: string
  containerName: string
  location: Location
}

export type SymbolsResult = {
  symbols: SymbolItem[]
}

export type InlayHint = {
  text: string
  position: number
  kind: 'type' | 'parameter' | 'enum'
}

export type InlayHintsResult = {
  hints: InlayHint[]
}

export type LinkedEditingResult = {
  ranges: Span[]
}

export type Typings = {
  libs: string[]
  roots: string[]
  files: Record<string, string>
}
