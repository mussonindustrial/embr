import ts from 'typescript'
import { files } from './workspace'
import { CodeAction, FileEdits, Location, TextEdit } from './protocol'

export function toLocation(
  program: ts.Program | undefined,
  fileName: string,
  span: ts.TextSpan
): Location {
  const source = program?.getSourceFile(fileName)
  const line = source?.getLineAndCharacterOfPosition(span.start).line ?? 0
  const lineStarts = source?.getLineStarts() ?? [0]
  const lineText = source?.text.slice(lineStarts[line], lineStarts[line + 1])

  return {
    path: files.pathOf(fileName),
    start: span.start,
    length: span.length,
    line: line + 1,
    lineText: lineText?.trim() ?? '',
  }
}

export function toTextEdit(change: ts.TextChange): TextEdit {
  return {
    start: change.span.start,
    length: change.span.length,
    newText: change.newText,
  }
}

function toFileEdits(changes: readonly ts.FileTextChanges[]): FileEdits[] {
  return changes.flatMap((change) => {
    const path = files.pathOf(change.fileName)
    return path == null
      ? []
      : [{ path, edits: change.textChanges.map(toTextEdit) }]
  })
}

export function toCodeAction(action: ts.CodeAction): CodeAction {
  return {
    description: action.description,
    changes: toFileEdits(action.changes),
  }
}
