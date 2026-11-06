import ts from 'typescript'
import { toLocation } from './convert'
import { isResource } from './files'
import { outlineOf } from './outline'
import { files, service, sourceFileOf, textOf } from './project'
import {
  DefinitionResult,
  FileRequest,
  HighlightsResult,
  OutlineResult,
  PositionRequest,
  ReferencesResult,
  SymbolsRequest,
  SymbolsResult,
} from './protocol'

const MAX_SYMBOLS = 100

export async function definition({
  path,
  offset,
}: PositionRequest): Promise<DefinitionResult | null> {
  const result = service.getDefinitionAndBoundSpan(
    files.fileNameOf(path),
    offset
  )
  if (result?.definitions == null || result.definitions.length === 0)
    return null

  const program = service.getProgram()
  return {
    span: result.textSpan,
    definitions: result.definitions.map((d) =>
      toLocation(program, d.fileName, d.textSpan)
    ),
  }
}

export async function references({
  path,
  offset,
}: PositionRequest): Promise<ReferencesResult> {
  const references =
    service.getReferencesAtPosition(files.fileNameOf(path), offset) ?? []
  const program = service.getProgram()
  return {
    references: references.map((r) =>
      toLocation(program, r.fileName, r.textSpan)
    ),
  }
}

export async function highlights({
  path,
  offset,
}: PositionRequest): Promise<HighlightsResult> {
  const fileName = files.fileNameOf(path)
  const highlights =
    service.getDocumentHighlights(fileName, offset, [fileName]) ?? []
  return {
    highlights: highlights
      .filter((h) => h.fileName === fileName)
      .flatMap((h) => h.highlightSpans)
      .map((span) => ({
        start: span.textSpan.start,
        length: span.textSpan.length,
        write: span.kind === ts.HighlightSpanKind.writtenReference,
      })),
  }
}

export async function outline({ path }: FileRequest): Promise<OutlineResult> {
  const fileName = files.fileNameOf(path)
  const sourceFile = sourceFileOf(fileName)
  if (sourceFile == null) return { items: [] }
  return { items: outlineOf(service.getNavigationTree(fileName), sourceFile) }
}

export async function symbols({
  query,
}: SymbolsRequest): Promise<SymbolsResult> {
  const program = service.getProgram()
  const items = service
    .getNavigateToItems(query, MAX_SYMBOLS, undefined, true)
    .filter((item) => isResource(item.fileName))

  return {
    symbols: items.map((item) => ({
      name: item.name,
      kind: item.kind,
      containerName: item.containerName,
      location: toLocation(program, item.fileName, nameSpanOf(item)),
    })),
  }
}

function nameSpanOf(item: ts.NavigateToItem): ts.TextSpan {
  const start = textOf(item.fileName).indexOf(item.name, item.textSpan.start)
  const inDeclaration = start >= 0 && start < ts.textSpanEnd(item.textSpan)
  return inDeclaration ? { start, length: item.name.length } : item.textSpan
}
