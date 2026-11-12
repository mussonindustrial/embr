import ts from 'typescript'
import * as parserTypeScript from 'prettier/parser-typescript'
import * as prettierPluginEstree from 'prettier/plugins/estree'

import { CompileResult, Compiler, CompilerRequest } from '../compiler/protocol'
import { errorDiagnostic, formatWith, toDiagnostic } from '../compiler/utils'

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
      result.diagnostics?.map((d) => toDiagnostic(source, d)) ?? []

    return {
      success: diagnostics.every((d) => d.severity !== 'error'),
      output: result.outputText,
      diagnostics,
    }
  } catch (error) {
    return {
      success: false,
      diagnostics: [errorDiagnostic('typescript', error)],
    }
  }
}

export const format = formatWith('typescript', [
  prettierPluginEstree as unknown as string,
  parserTypeScript,
])

export const compiler: Compiler = { compile, format }
