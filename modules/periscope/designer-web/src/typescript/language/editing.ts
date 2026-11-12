import ts from 'typescript'
import { toTextEdit } from './convert'
import {
  files,
  formatSettings,
  preferences,
  service,
  textOf,
} from './workspace'
import {
  EditsResult,
  FileRequest,
  FormatOnTypeRequest,
  JsxClosingTagResult,
  LinkedEditingResult,
  PositionRequest,
} from './protocol'

export async function formatOnType({
  path,
  offset,
  key,
}: FormatOnTypeRequest): Promise<EditsResult> {
  const fileName = files.fileNameOf(path)
  const edits = service.getFormattingEditsAfterKeystroke(
    fileName,
    offset,
    key,
    formatSettings
  )
  const indentation = key === '\n' ? [indentLine(fileName, offset)] : []
  return { edits: [...edits, ...indentation].map(toTextEdit) }
}

function indentLine(fileName: string, offset: number): ts.TextChange {
  const text = textOf(fileName)
  const lineStart = text.lastIndexOf('\n', offset - 1) + 1
  const indentEnd = lineStart + /^[ \t]*/.exec(text.slice(lineStart))![0].length
  const indentation = service.getIndentationAtPosition(
    fileName,
    indentEnd,
    formatSettings
  )
  return {
    span: { start: lineStart, length: indentEnd - lineStart },
    newText: ' '.repeat(indentation),
  }
}

export async function linkedEditing({
  path,
  offset,
}: PositionRequest): Promise<LinkedEditingResult> {
  const fileName = files.fileNameOf(path)
  const linked = service.getLinkedEditingRangeAtPosition(fileName, offset)
  return { ranges: linked?.ranges ?? [] }
}

export async function jsxClosingTag({
  path,
  offset,
}: PositionRequest): Promise<JsxClosingTagResult | null> {
  return (
    service.getJsxClosingTagAtPosition(files.fileNameOf(path), offset) ?? null
  )
}

export async function organizeImports({
  path,
}: FileRequest): Promise<EditsResult> {
  const fileName = files.fileNameOf(path)
  const changes = service.organizeImports(
    { type: 'file', fileName },
    formatSettings,
    preferences
  )
  return {
    edits: changes
      .filter((change) => change.fileName === fileName)
      .flatMap((change) => change.textChanges.map(toTextEdit)),
  }
}
