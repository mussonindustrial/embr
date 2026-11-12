import ts from 'typescript'
import { toLocation } from './convert'
import { isResource } from './files'
import { outlineOf } from './outline'
import { files, service, sourceFileOf, textOf } from './workspace'
import {
  DefinitionResult,
  ExportsResult,
  FileRequest,
  HighlightsResult,
  OutlineResult,
  PositionRequest,
  ReferencesResult,
  SymbolsRequest,
  SymbolsResult,
} from './protocol'

const MAX_SYMBOLS = 100
const REACT_COMPONENT = 'ReactComponent'

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
      .map((span) => span.textSpan),
  }
}

export async function outline({ path }: FileRequest): Promise<OutlineResult> {
  const fileName = files.fileNameOf(path)
  const sourceFile = sourceFileOf(fileName)
  if (sourceFile == null) return { items: [] }
  return { items: outlineOf(service.getNavigationTree(fileName), sourceFile) }
}

export async function reactComponentNames({
  path,
}: FileRequest): Promise<ExportsResult> {
  const checker = service.getProgram()?.getTypeChecker()
  const sourceFile = sourceFileOf(files.fileNameOf(path))
  const module = sourceFile && checker?.getSymbolAtLocation(sourceFile)
  if (checker == null || module == null) return { names: [] }

  const isReactComponent = (symbol: ts.Symbol) => {
    const isAlias = symbol.flags & ts.SymbolFlags.Alias
    const target = isAlias ? checker.getAliasedSymbol(symbol) : symbol
    if (!(target.flags & ts.SymbolFlags.Value)) return false

    const type = checker.getTypeOfSymbol(target).aliasSymbol
    return (
      type?.name === REACT_COMPONENT &&
      type.declarations?.some(
        (declaration) => !isResource(declaration.getSourceFile().fileName)
      )
    )
  }
  return {
    names: checker
      .getExportsOfModule(module)
      .filter(isReactComponent)
      .map((symbol) => symbol.name),
  }
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
