import ts from 'typescript'
import * as prettier from 'prettier/standalone'
import * as parserTypeScript from 'prettier/parser-typescript'
import * as prettierPluginEstree from 'prettier/plugins/estree'

import {
  CompileResult,
  Compiler,
  CompilerRequest,
  Diagnostic,
  FormatResult,
} from '../protocol'
import { positionOf } from '../utils'

function mapDiagnostic(source: string, diagnostic: ts.Diagnostic): Diagnostic {
  let range = undefined

  if (diagnostic.start != null && diagnostic.length != null) {
    range = {
      start: positionOf(source, diagnostic.start),
      end: positionOf(source, diagnostic.start + diagnostic.length),
    }
  }

  return {
    severity:
      diagnostic.category === ts.DiagnosticCategory.Error ? 'error' : 'warning',
    message: ts.flattenDiagnosticMessageText(diagnostic.messageText, '\n'),
    code: String(diagnostic.code),
    source: 'typescript',
    range,
  }
}

export async function compile({
  source,
  path,
}: CompilerRequest): Promise<CompileResult> {
  try {
    const result = ts.transpileModule(source, {
      fileName: path,
      reportDiagnostics: true,
      compilerOptions: {
        module: ts.ModuleKind.ESNext,
        target: ts.ScriptTarget.ES2020,
        strict: true,
        jsx: ts.JsxEmit.React,
      },
    })

    const diagnostics =
      result.diagnostics?.map((d) => mapDiagnostic(source, d)) ?? []

    return {
      success: diagnostics.every((d) => d.severity !== 'error'),
      output: result.outputText,
      diagnostics,
    }
  } catch (error) {
    return {
      success: false,
      diagnostics: [
        {
          severity: 'error',
          source: 'typescript',
          message: error instanceof Error ? error.message : String(error),
          stack: error instanceof Error ? error.stack : undefined,
        },
      ],
    }
  }
}

function mapError(error: unknown): Diagnostic {
  if (error instanceof Error) {
    return {
      severity: 'error',
      source: 'prettier',
      message: error.message,
      stack: error.stack,
    }
  }

  return {
    severity: 'error',
    source: 'prettier',
    message: String(error),
  }
}

export async function format({
  source,
  cursor,
}: CompilerRequest & {
  cursor: number
}): Promise<FormatResult> {
  try {
    const result = await prettier.formatWithCursor(source, {
      endOfLine: 'lf',
      cursorOffset: cursor,
      parser: 'typescript',
      printWidth: 120,
      plugins: [prettierPluginEstree as unknown as string, parserTypeScript],
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
      diagnostics: [mapError(error)],
    }
  }
}

export const compiler: Compiler = {
  compile: compile,
  format: format,
}
