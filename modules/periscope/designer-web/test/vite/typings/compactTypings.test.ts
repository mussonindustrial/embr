import { describe, expect, it } from 'vitest'
import { compactTypings } from '../../../vite/typings/compactTypings'

describe('Typings Plugin', () => {
  describe('compactTypings()', () => {
    it('collapses whitespace between tokens', async () => {
      const text = 'interface A {\n    a:   string\n\n    b: number\n}\n'

      expect(compactTypings(text, true)).toBe(
        'interface A {\na: string\nb: number\n}\n'
      )
    })

    it('keeps the text of strings and template literal types', async () => {
      const text = "type A = `a  ${'b  c'}  d${`  e`}`\ntype B = 'f  g'\n"

      expect(compactTypings(text, true)).toBe(text)
    })

    it('drops MDN links from doc comments', async () => {
      const text =
        '/**\n     * Fires on click.\n     *\n     * [MDN Reference](https://developer.mozilla.org/a)\n     */\nx: 1\n'

      expect(compactTypings(text, true)).toBe(
        '/**\n * Fires on click.\n */\nx: 1\n'
      )
    })

    it('drops comments without docs, but keeps triple-slash directives', async () => {
      const text =
        '/// <reference path="a.d.ts" />\n/** Doc. */\n// Note.\nexport type A = 1\n'

      expect(compactTypings(text, false)).toBe(
        '/// <reference path="a.d.ts" />\nexport type A = 1\n'
      )
    })
  })
})
