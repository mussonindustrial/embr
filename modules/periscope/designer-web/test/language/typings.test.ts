import typings from 'virtual:periscope-typings'
import ts from 'typescript'
import { describe, expect, it } from 'vitest'
import { ProjectFiles } from '../../src/language/files'
import { createHost } from '../../src/language/host'

// Imported by the bundled typings but left out, so they type as `any`.
const UNBUNDLED = [
  'axios',
  'history',
  'jsnlog',
  'mobx',
  'moment',
  'moment-timezone',
  'node',
  'react-resize-detector/build/withPolyfill',
  'react-to-print',
  'transformation-matrix',
]

function unresolvedImports() {
  const host = createHost(new ProjectFiles(), typings)
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

describe('Typings Asset', () => {
  it('resolves every import but the unbundled packages', () => {
    expect([...new Set(unresolvedImports())].sort()).toEqual(UNBUNDLED)
  })

  it('starts from roots it holds', () => {
    typings.roots.forEach((root) =>
      expect(typings.files).toHaveProperty([root])
    )
  })
})
