import { describe, expect, it } from 'vitest'
import { replaceImports } from '../../src/stores/moduleUrls'

describe('replaceImports', () => {
  it('replaces only the specifiers at the given positions', () => {
    const source = `import a from '/a'; const text = "from '/a'"; import('/b')`
    const first = source.indexOf('/a')
    const second = source.lastIndexOf('/b')

    const replaced = replaceImports(
      source,
      [
        [first, first + 2],
        [second, second + 2],
      ],
      (specifier) => `https://host${specifier}`
    )

    expect(replaced).toBe(
      `import a from 'https://host/a'; const text = "from '/a'"; import('https://host/b')`
    )
  })

  it('returns the source when there are no imports', () => {
    expect(replaceImports('const a = 1', [], () => 'unused')).toBe(
      'const a = 1'
    )
  })
})
