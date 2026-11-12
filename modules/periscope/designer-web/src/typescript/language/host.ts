import ts from 'typescript'
import { WorkspaceFiles, SYSTEM_MODULES } from './files'
import { Typings } from './protocol'

const DEFAULT_LIB = '/lib/lib.d.ts'

const compilerOptions: ts.CompilerOptions = {
  target: ts.ScriptTarget.ES2020,
  module: ts.ModuleKind.ESNext,
  moduleResolution: ts.ModuleResolutionKind.Bundler,
  moduleDetection: ts.ModuleDetectionKind.Force,
  jsx: ts.JsxEmit.React,
  types: [],
  strict: true,
  noEmit: true,
  isolatedModules: true,
  esModuleInterop: true,
  allowUmdGlobalAccess: true,
  allowJs: true,
  skipLibCheck: true,
  baseUrl: '/',
  paths: { '@/*': ['./src/*'], 'system:*': [`.${SYSTEM_MODULES}*`] },
}

export function createHost(
  files: WorkspaceFiles,
  typings: Typings
): ts.LanguageServiceHost {
  const typingFileNames = Object.keys(typings.files)
  const fileNames = () => [...typingFileNames, ...files.fileNames]

  const readFile = (fileName: string) =>
    files.get(fileName)?.text ?? typings.files[fileName]

  const childrenOf = (directory: string) =>
    fileNames()
      .filter((fileName) => fileName.startsWith(prefixOf(directory)))
      .map((fileName) => fileName.slice(prefixOf(directory).length))

  return {
    getCompilationSettings: () => ({ ...compilerOptions, lib: typings.libs }),
    getScriptFileNames: () => [...typings.roots, ...files.fileNames],
    getScriptVersion: (fileName) => String(files.get(fileName)?.version ?? 0),
    getScriptSnapshot: (fileName) => {
      const text = readFile(fileName)
      return text == null ? undefined : ts.ScriptSnapshot.fromString(text)
    },
    getCurrentDirectory: () => '/',
    getDefaultLibFileName: () => DEFAULT_LIB,
    useCaseSensitiveFileNames: () => true,
    fileExists: (fileName) => readFile(fileName) != null,
    readFile,
    directoryExists: (directory) =>
      fileNames().some((fileName) => fileName.startsWith(prefixOf(directory))),
    getDirectories: (directory) => [
      ...new Set(
        childrenOf(directory)
          .filter((child) => child.includes('/'))
          .map((child) => child.slice(0, child.indexOf('/')))
      ),
    ],
    readDirectory: (directory, extensions) =>
      childrenOf(directory)
        .filter((child) => !child.includes('/'))
        .filter(
          (child) => !extensions || extensions.some((e) => child.endsWith(e))
        )
        .map((child) => prefixOf(directory) + child),
  }
}

function prefixOf(directory: string) {
  return directory.replace(/\/*$/, '/')
}
