import typings from 'virtual:periscope-typings'
import ts from 'typescript'
import { beforeAll, describe, expect, it } from 'vitest'
import { WorkspaceFiles } from '../../../src/typescript/language/files'
import { createHost } from '../../../src/typescript/language/host'

const PERSPECTIVE = '@inductiveautomation/'

function unresolvedImports() {
  const host = createHost(new WorkspaceFiles(), typings)
  const options = host.getCompilationSettings()

  return Object.entries(typings.files)
    .filter(([path]) => path.endsWith('.d.ts'))
    .flatMap(([path, text]) => {
      const { importedFiles, typeReferenceDirectives } = ts.preProcessFile(
        text,
        true,
        true
      )
      return [...importedFiles, ...typeReferenceDirectives]
        .map((reference) => reference.fileName)
        .filter((specifier) => !/\.s?css$/.test(specifier))
        .filter(
          (specifier) =>
            ts.resolveModuleName(specifier, path, options, host)
              .resolvedModule == null
        )
    })
}

function bundledPackages() {
  return new Set(
    Object.keys(typings.files)
      .map((path) => /^\/node_modules\/((?:@[^/]+\/)?[^/]+)\//.exec(path)?.[1])
      .filter((name) => name != null)
  )
}

function packageName(specifier: string) {
  const parts = specifier.split('/')
  return specifier.startsWith('@') ? parts.slice(0, 2).join('/') : parts[0]
}

describe('Typings Asset', () => {
  let unresolved: string[]

  beforeAll(() => {
    unresolved = [...new Set(unresolvedImports())].sort()
  })

  it('resolves every relative import', () => {
    expect(unresolved.filter((specifier) => specifier.startsWith('.'))).toEqual(
      []
    )
  })

  it("resolves every import of Perspective's own packages", () => {
    expect(
      unresolved.filter((specifier) => specifier.startsWith(PERSPECTIVE))
    ).toEqual([])
  })

  it('resolves every import of the packages it bundles', () => {
    const bundled = bundledPackages()
    expect(
      unresolved.filter((specifier) => bundled.has(packageName(specifier)))
    ).toEqual([])
  })

  it('starts from roots it holds', () => {
    typings.roots.forEach((root) =>
      expect(typings.files).toHaveProperty([root])
    )
  })
})
