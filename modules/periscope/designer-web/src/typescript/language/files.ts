import { ResourceFile, ResourceKind } from './protocol'
import { cssModuleDeclaration } from './cssModuleDeclaration'

export type ProjectFile = {
  path: string
  text: string
  version: number
}

const extensions: Record<ResourceKind, string> = {
  typescript: '.tsx',
  javascript: '.jsx',
  css: '.d.ts',
  component: '.tsx',
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

function toText({ kind, source }: ResourceFile) {
  return kind === 'css' ? cssModuleDeclaration(source) : source
}

export class WorkspaceFiles {
  private readonly files = new Map<string, ProjectFile>()
  private readonly fileNamesByPath = new Map<string, string>()
  private readonly components = new Set<string>()
  private nextVersion = 1

  get fileNames() {
    return [...this.files.keys()]
  }

  get componentPaths() {
    return [...this.components]
  }

  get(fileName: string) {
    return this.files.get(fileName)
  }

  toFileName(path: string) {
    return this.fileNamesByPath.get(path) ?? fileNameFor(path, 'typescript')
  }

  findPath(fileName: string): string | null {
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
    this.components.delete(path)
  }

  update(file: ResourceFile) {
    const fileName = fileNameFor(file.path, file.kind)
    const previous = this.fileNamesByPath.get(file.path)
    if (previous != null && previous !== fileName) {
      this.remove(file.path)
    }
    this.fileNamesByPath.set(file.path, fileName)
    if (file.kind === 'component') {
      this.components.add(file.path)
    } else {
      this.components.delete(file.path)
    }
    const text = toText(file)
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
