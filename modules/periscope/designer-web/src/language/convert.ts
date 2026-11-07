import ts from 'typescript'
import { Diagnostic } from '../compiler/protocol'
import { positionOf } from '../compiler/utils'
import { files } from './project'
import { CodeAction, FileEdits, Location, TextEdit } from './protocol'

export function displayText(parts: ts.SymbolDisplayPart[] | undefined) {
  return ts.displayPartsToString(parts)
}

export function toLocation(
  program: ts.Program | undefined,
  fileName: string,
  span: ts.TextSpan
): Location {
  const source = program?.getSourceFile(fileName)
  const { line, character } = source?.getLineAndCharacterOfPosition(
    span.start
  ) ?? { line: 0, character: 0 }
  const lineStarts = source?.getLineStarts() ?? [0]
  const lineText = source?.text.slice(lineStarts[line], lineStarts[line + 1])

  return {
    path: files.pathOf(fileName),
    start: span.start,
    length: span.length,
    line: line + 1,
    column: character + 1,
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

export function toFileEdits(
  changes: readonly ts.FileTextChanges[]
): FileEdits[] {
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

function severityOf(diagnostic: ts.Diagnostic): Diagnostic['severity'] {
  switch (diagnostic.category) {
    case ts.DiagnosticCategory.Error:
      return 'error'
    case ts.DiagnosticCategory.Warning:
      return 'warning'
    default:
      return 'info'
  }
}

export function toDiagnostic(
  source: string,
  diagnostic: ts.Diagnostic
): Diagnostic {
  const start = diagnostic.start ?? 0
  const length = diagnostic.length ?? 0

  return {
    severity: severityOf(diagnostic),
    message: ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n'),
    code: String(diagnostic.code),
    source: 'typescript',
    range: {
      start: positionOf(source, start),
      end: positionOf(source, start + length),
    },
  }
}
