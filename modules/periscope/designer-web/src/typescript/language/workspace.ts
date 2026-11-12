import ts from 'typescript'
import {
  isResource,
  WorkspaceFiles,
  SYSTEM_MODULES,
  TYPINGS_ROOT,
} from './files'
import { GLOBAL_MODULES } from './globals'
import { createHost } from './host'
import { FileRequest, ResourceFile, SetFilesRequest, Typings } from './protocol'

export const files = new WorkspaceFiles()
let typings: Typings = { libs: [], roots: [], files: {} }
export let service = createService()

function createService() {
  return ts.createLanguageService(createHost(files, typings))
}

export const formatSettings: ts.FormatCodeSettings = {
  ...ts.getDefaultFormatCodeSettings('\n'),
  indentSize: 2,
  tabSize: 2,
  convertTabsToSpaces: true,
  semicolons: ts.SemicolonPreference.Ignore,
  insertSpaceAfterFunctionKeywordForAnonymousFunctions: true,
}

const importablePackages = GLOBAL_MODULES.map((module) => module.specifier)

export const preferences: ts.UserPreferences = {
  quotePreference: 'single',
  importModuleSpecifierPreference: 'non-relative',
  includeCompletionsForModuleExports: true,
  includeCompletionsForImportStatements: true,
  includeCompletionsWithInsertText: true,
  includeAutomaticOptionalChainCompletions: true,
  includePackageJsonAutoImports: 'off',
  allowIncompleteCompletions: false,
  autoImportFileExcludePatterns: [`${TYPINGS_ROOT}**`],
  autoImportSpecifierExcludeRegexes: [
    `^(?!@/|(?:${importablePackages.join('|')})$)`,
  ],
}

const embeddedPreferences: ts.UserPreferences = {
  ...preferences,
  autoImportFileExcludePatterns: ['**/*'],
}

export function preferencesFor(fileName: string) {
  return isResource(fileName) ? preferences : embeddedPreferences
}

export function textOf(fileName: string) {
  return files.get(fileName)?.text ?? ''
}

export function systemModules() {
  return Object.keys(typings.files)
    .filter((fileName) => fileName.startsWith(SYSTEM_MODULES))
    .map((fileName) => fileName.slice(SYSTEM_MODULES.length))
    .filter((name) => name.endsWith('.d.ts') && !name.includes('/'))
    .map((name) => name.slice(0, -'.d.ts'.length))
}

export function sourceFileOf(fileName: string) {
  return service.getProgram()?.getSourceFile(fileName)
}

export async function setTypings(next: Typings) {
  typings = next
  service = createService()
}

export async function setFiles({ files: resources }: SetFilesRequest) {
  files.set(resources)
}

export async function updateFile(file: ResourceFile) {
  files.update(file)
}

export async function removeFile({ path }: FileRequest) {
  files.remove(path)
}

export async function warmUp() {
  service.getProgram()
}
