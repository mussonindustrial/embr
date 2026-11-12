import { createRequire } from 'node:module'
import { describe, expect, it } from 'vitest'
import { GLOBAL_MODULES } from '../../src/typescript/language/globals'

describe('Global Modules', () => {
  it('installs the versions Perspective ships', () => {
    const require = createRequire(import.meta.url)
    const installed = GLOBAL_MODULES.map(({ specifier }) => [
      specifier,
      require(`${specifier}/package.json`).version,
    ])

    expect(installed).toEqual(
      GLOBAL_MODULES.map(({ specifier, version }) => [specifier, version])
    )
  })
})
