import { importDiagnostics } from './browserImports'
import { toDiagnostic } from '../../compiler/utils'
import { toCodeAction } from './convert'
import { isResource } from './files'
import {
  files,
  formatSettings,
  preferencesFor,
  service,
  sourceFileOf,
  textOf,
} from './workspace'
import {
  CodeFixesRequest,
  CodeFixesResult,
  DiagnosticsResult,
  FileRequest,
} from './protocol'

export async function diagnostics({
  path,
}: FileRequest): Promise<DiagnosticsResult> {
  const fileName = files.fileNameOf(path)
  const sourceFile = sourceFileOf(fileName)
  const unnecessary = service
    .getSuggestionDiagnostics(fileName)
    .filter((d) => d.reportsUnnecessary || d.reportsDeprecated)
  const syntactic = isResource(fileName)
    ? []
    : service.getSyntacticDiagnostics(fileName)
  const semantic = service.getSemanticDiagnostics(fileName)
  const imports = sourceFile
    ? importDiagnostics(sourceFile, semantic)
    : semantic

  return {
    diagnostics: [...syntactic, ...imports, ...unnecessary].map((d) =>
      toDiagnostic(textOf(fileName), d)
    ),
  }
}

export async function codeFixes({
  path,
  start,
  end,
}: CodeFixesRequest): Promise<CodeFixesResult> {
  const fileName = files.fileNameOf(path)
  const errorCodes = [
    ...service.getSyntacticDiagnostics(fileName),
    ...service.getSemanticDiagnostics(fileName),
    ...service.getSuggestionDiagnostics(fileName),
  ]
    .filter(
      (d) =>
        d.start != null && d.start <= end && d.start + (d.length ?? 0) >= start
    )
    .map((d) => d.code)

  const fixes = service.getCodeFixesAtPosition(
    fileName,
    start,
    end,
    [...new Set(errorCodes)],
    formatSettings,
    preferencesFor(fileName)
  )
  return { actions: fixes.map(toCodeAction) }
}
