import { ResourceFile, ResourceKind } from './protocol'
import { cssModuleDeclaration } from './cssModuleDeclaration'

type ProjectFile = {
  path: string
  text: string
  version: number
}

const extensions: Record<ResourceKind, string> = {
  typescript: '.tsx',
  javascript: '.jsx',
  css: '.d.ts',
}

export const RESOURCE_ROOT = '/src/'
export const TYPINGS_ROOT = '/$typings/'
export const SYSTEM_MODULES = `${TYPINGS_ROOT}system/`

const EMBEDDED = '__embedded__/'

function fileNameFor(path: string, kind: ResourceKind) {
  const directory = path.startsWith(EMBEDDED) ? '/' : RESOURCE_ROOT
  return `${directory}${path}${extensions[kind]}`
}

export function isResource(fileName: string) {
  return fileName.startsWith(RESOURCE_ROOT)
}

function textOf({ kind, source }: ResourceFile) {
  return kind === 'css' ? cssModuleDeclaration(source) : source
}

export class WorkspaceFiles {
  private readonly files = new Map<string, ProjectFile>()
  private readonly fileNamesByPath = new Map<string, string>()
  private nextVersion = 1

  get fileNames() {
    return [...this.files.keys()]
  }

  get(fileName: string) {
    return this.files.get(fileName)
  }

  fileNameOf(path: string) {
    return this.fileNamesByPath.get(path) ?? fileNameFor(path, 'typescript')
  }

  pathOf(fileName: string): string | null {
    return this.files.get(fileName)?.path ?? null
  }

  set(files: ResourceFile[]) {
    const paths = new Set(files.map((file) => file.path))
    ;[...this.fileNamesByPath.keys()]
      .filter((path) => !paths.has(path))
      .forEach((path) => this.remove(path))
    files.forEach((file) => this.update(file))
  }

  remove(path: string) {
    const fileName = this.fileNamesByPath.get(path)
    if (fileName == null) return
    this.files.delete(fileName)
    this.fileNamesByPath.delete(path)
  }

  update(file: ResourceFile) {
    const fileName = fileNameFor(file.path, file.kind)
    const previous = this.fileNamesByPath.get(file.path)
    if (previous != null && previous !== fileName) {
      this.remove(file.path)
    }
    this.fileNamesByPath.set(file.path, fileName)
    const text = textOf(file)
    if (this.files.get(fileName)?.text === text) {
      return
    }
    this.files.set(fileName, {
      path: file.path,
      text,
      version: this.nextVersion++,
    })
  }
}
