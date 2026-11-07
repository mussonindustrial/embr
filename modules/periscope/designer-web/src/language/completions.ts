import ts from 'typescript'
import { toCodeAction } from './convert'
import { documentationHtml } from './documentation'
import { importCompletions } from './importCompletions'
import {
  files,
  formatSettings,
  preferencesFor,
  service,
  sourceFileOf,
  systemModules,
  textOf,
} from './project'
import {
  CompletionDetailsRequest,
  CompletionDetailsResult,
  CompletionEntry,
  CompletionsResult,
  PositionRequest,
} from './protocol'
import { signatureText } from './signature'

export async function completions({
  path,
  offset,
  prefix = '',
}: PositionRequest & { prefix?: string }): Promise<CompletionsResult> {
  const fileName = files.fileNameOf(path)
  const result = service.getCompletionsAtPosition(
    fileName,
    offset,
    preferencesFor(fileName)
  )
  if (result == null) {
    return { entries: [], isMemberCompletion: false }
  }

  const text = textOf(fileName)
  const enteredText = (entry: ts.CompletionEntry) =>
    entry.replacementSpan
      ? text.slice(entry.replacementSpan.start, offset)
      : prefix

  const sourceFile = sourceFileOf(fileName)
  const available = sourceFile
    ? importCompletions(sourceFile, offset, result.entries, systemModules())
    : result.entries
  const entries = available
    .filter((entry) => matchesPrefix(entry.name, enteredText(entry)))
    .sort(compareEntries(prefix))
    .map(toCompletionEntry)

  return { entries, isMemberCompletion: result.isMemberCompletion }
}

export async function completionDetails({
  path,
  offset,
  name,
  source,
  data,
}: CompletionDetailsRequest): Promise<CompletionDetailsResult | null> {
  const fileName = files.fileNameOf(path)
  const details = service.getCompletionEntryDetails(
    fileName,
    offset,
    name,
    formatSettings,
    source ?? undefined,
    preferencesFor(fileName),
    (data ?? undefined) as ts.CompletionEntryData | undefined
  )
  if (details == null) return null

  return {
    display: signatureText(details.displayParts),
    documentationHtml: documentationHtml(details.documentation, details.tags),
    codeActions: details.codeActions?.map(toCodeAction) ?? [],
  }
}

function matchesPrefix(name: string, prefix: string) {
  if (name.toLowerCase().startsWith(prefix.toLowerCase())) return true
  return matchesHumps(name, prefix)
}

function matchesHumps(name: string, prefix: string) {
  const lowerName = name.toLowerCase()
  const lowerPrefix = prefix.toLowerCase()
  const humps = humpStarts(name)
  const matches = (at: number, index: number): boolean => {
    if (index === lowerPrefix.length) return true
    if (lowerName[at] !== lowerPrefix[index]) return false
    return (
      matches(at + 1, index + 1) ||
      humps.some((hump) => hump > at && matches(hump, index + 1))
    )
  }
  return matches(0, 0)
}

function humpStarts(name: string) {
  return [...name]
    .map((char, i) =>
      i > 0 && (/[A-Z_$]/.test(char) || name[i - 1] === '_') ? i : -1
    )
    .filter((i) => i > 0)
}

function initialsOf(name: string) {
  return (
    name[0] +
    humpStarts(name)
      .map((i) => name[i])
      .join('')
  )
}

function compareEntries(prefix: string) {
  const lowerPrefix = prefix.toLowerCase()
  const rank = ({ name }: ts.CompletionEntry) => {
    if (name.toLowerCase().startsWith(lowerPrefix)) return 0
    if (initialsOf(name).toLowerCase().startsWith(lowerPrefix)) return 1
    return 2
  }
  return (a: ts.CompletionEntry, b: ts.CompletionEntry) =>
    rank(a) - rank(b) ||
    a.sortText.localeCompare(b.sortText) ||
    a.name.localeCompare(b.name)
}

function toCompletionEntry(entry: ts.CompletionEntry): CompletionEntry {
  return {
    name: entry.name,
    kind: entry.kind,
    kindModifiers: entry.kindModifiers ?? '',
    sortText: entry.sortText,
    insertText: entry.insertText,
    replacementSpan: entry.replacementSpan,
    source: entry.source,
    data: entry.data,
    hasAction: entry.hasAction ?? false,
    isSnippet: entry.isSnippet ?? false,
  }
}
