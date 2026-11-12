import { Diagnostic } from '../../compiler/protocol'

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

export type Location = Span & {
  path: string | null
  line: number
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

export type TokenKind =
  | 'TEXT'
  | 'KEYWORD'
  | 'LINE_COMMENT'
  | 'BLOCK_COMMENT'
  | 'DOC_COMMENT'
  | 'DOC_COMMENT_TAG'
  | 'STRING'
  | 'NUMBER'
  | 'REGEX'
  | 'OPERATOR'
  | 'BRACKET'
  | 'PUNCTUATION'
  | 'TYPE'
  | 'FUNCTION'
  | 'PARAMETER'
  | 'VARIABLE'
  | 'CONSTANT'
  | 'PROPERTY'
  | 'JSX_TAG'
  | 'JSX_ATTRIBUTE'
  | 'JSX_TEXT'

export type ClassificationsResult = {
  kinds: TokenKind[]
  tokens: number[]
}

export type DiagnosticsResult = {
  diagnostics: Diagnostic[]
}

export type CompletionEntry = {
  name: string
  kind: string
  kindModifiers: string
  insertText?: string
  replacementSpan?: Span
  source?: string
  data?: unknown
  hasAction: boolean
}

export type CompletionsResult = {
  entries: CompletionEntry[]
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
  highlights: Span[]
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

export type ExportsResult = {
  names: string[]
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
