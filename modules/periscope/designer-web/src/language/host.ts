import ts from 'typescript'
import { ProjectFiles } from './files'
import { Typings } from './protocol'

// The language service looks for the `lib` files beside the default one.
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
  paths: { '@/*': ['./src/*'], 'system:*': ['./system/*'] },
}

export function createHost(
  files: ProjectFiles,
  typings: Typings
): ts.LanguageServiceHost {
  const typingFileNames = Object.keys(typings.files)
  const typingDirectories = directoriesOf(typingFileNames)

  const readFile = (fileName: string) =>
    files.get(fileName)?.text ?? typings.files[fileName]

  const directoryExists = (directory: string) => {
    const name = trimSlash(directory)
    return (
      typingDirectories.has(name) || directoriesOf(files.fileNames).has(name)
    )
  }

  const childrenOf = (directory: string) => {
    const prefix =
      trimSlash(directory) === '/' ? '/' : trimSlash(directory) + '/'
    return [...typingFileNames, ...files.fileNames]
      .filter((fileName) => fileName.startsWith(prefix))
      .map((fileName) => fileName.slice(prefix.length))
  }

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
    directoryExists,
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
        .map(
          (child) =>
            `${trimSlash(directory) === '/' ? '' : trimSlash(directory)}/${child}`
        ),
  }
}

function directoriesOf(fileNames: string[]) {
  const directories = new Set<string>()
  fileNames.forEach((fileName) =>
    parentDirectories(fileName).forEach((directory) =>
      directories.add(directory)
    )
  )
  return directories
}

function parentDirectories(fileName: string) {
  const parts = fileName.split('/').slice(1, -1)
  return ['/', ...parts.map((_, i) => '/' + parts.slice(0, i + 1).join('/'))]
}

function trimSlash(directory: string) {
  return directory.length > 1 ? directory.replace(/\/$/, '') : directory
}
