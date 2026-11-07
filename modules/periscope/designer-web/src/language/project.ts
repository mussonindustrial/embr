import ts from 'typescript'
import { isResource, ProjectFiles } from './files'
import { createHost } from './host'
import { FileRequest, ResourceFile, SetFilesRequest, Typings } from './protocol'

export const files = new ProjectFiles()
let typings: Typings = { libs: [], roots: [], files: {} }
const documents = ts.createDocumentRegistry()
export let service = createService()

function createService() {
  return ts.createLanguageService(createHost(files, typings), documents)
}

export const formatSettings: ts.FormatCodeSettings = {
  ...ts.getDefaultFormatCodeSettings('\n'),
  indentSize: 2,
  tabSize: 2,
  convertTabsToSpaces: true,
  semicolons: ts.SemicolonPreference.Ignore,
  insertSpaceAfterFunctionKeywordForAnonymousFunctions: true,
}

export const preferences: ts.UserPreferences = {
  quotePreference: 'single',
  importModuleSpecifierPreference: 'non-relative',
  includeCompletionsForModuleExports: true,
  includeCompletionsForImportStatements: true,
  includeCompletionsWithInsertText: true,
  includeAutomaticOptionalChainCompletions: true,
  includePackageJsonAutoImports: 'off',
  allowIncompleteCompletions: false,
  autoImportFileExcludePatterns: [
    '/node_modules/**',
    '/globals/**',
    '/declarations/**',
    '/system/**',
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
    .map((fileName) => /^\/system\/([^/]+)\.d\.ts$/.exec(fileName)?.[1])
    .filter((name) => name != null)
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
  return {}
}

export async function updateFile(file: ResourceFile) {
  files.update(file)
  return {}
}

export async function removeFile({ path }: FileRequest) {
  files.remove(path)
  return {}
}

export async function warmUp() {
  service.getProgram()
  return {}
}
