import type { Plugin } from 'prettier'
import * as prettier from 'prettier/standalone'
import ts from 'typescript'
import { CompilerRequest, Diagnostic, FormatResult, Position } from './protocol'

export function positionOf(source: string, offset: number): Position {
  const lines = source.slice(0, offset).split('\n')

  const line = lines.length
  const column = lines[lines.length - 1].length + 1

  return {
    line,
    column,
    offset,
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

export function errorDiagnostic(source: string, error: unknown): Diagnostic {
  return {
    severity: 'error',
    source,
    message: error instanceof Error ? error.message : String(error),
    stack: error instanceof Error ? error.stack : undefined,
  }
}

export function formatWith(parser: string, plugins: (string | Plugin)[]) {
  return async ({
    source,
    cursor,
  }: CompilerRequest & { cursor: number }): Promise<FormatResult> => {
    try {
      const result = await prettier.formatWithCursor(source, {
        endOfLine: 'lf',
        cursorOffset: cursor,
        parser,
        printWidth: 120,
        plugins,
      })

      return {
        success: true,
        output: result.formatted,
        cursor: result.cursorOffset,
        diagnostics: [],
      }
    } catch (error) {
      return {
        success: false,
        output: source,
        cursor,
        diagnostics: [errorDiagnostic('prettier', error)],
      }
    }
  }
}
