import { describe, expect, it } from 'vitest'
import { collectTypings } from '../../../vite/typings/collectTypings'

describe('Typings Plugin', () => {
  describe('collectTypings()', () => {
    it('throws when a package is not installed', async () => {
      const manifest = {
        libs: [],
        packages: [{ name: 'not-a-package' }],
        globals: {},
        systemModules: {},
      }

      expect(() => collectTypings(manifest, process.cwd())).toThrow(
        "Package 'not-a-package' isn't installed, or nothing uses it."
      )
    })
  })
})
