import { describe, expect, it } from 'vitest'

import { compiler } from '../../src/css/compiler'
import { cssModuleDeclaration } from '../../src/typescript/language/cssModuleDeclaration'

function compiledClassNames(output = '') {
  const styles = /^export const styles = (.*)$/m.exec(output)?.[1] ?? '{}'
  return Object.keys(JSON.parse(styles)).sort()
}

function declaredClassNames(source: string) {
  const members = cssModuleDeclaration(source).matchAll(
    /readonly "([^"]+)": string/g
  )
  return [...members].map((member) => member[1]).sort()
}

describe('CSS Module Compiler', () => {
  describe('compile()', () => {
    it('exports the class names the editor declares', async () => {
      const source = [
        '.my-button { color: red; }',
        '.card .title:hover > .icon::after { color: blue; }',
        ':global(.shared) .inner { color: green; }',
        '/* .commented { } */',
        '.image { background: url(a.b.png); content: ".quoted"; }',
        '@media (min-width: 1px) { .wide { color: red; } }',
      ].join('\r\n')

      const result = await compiler.compile({ source, path: 'styles.css' })

      expect(result.diagnostics).toEqual([])
      expect(compiledClassNames(result.output)).toEqual([
        'card',
        'icon',
        'image',
        'inner',
        'my-button',
        'myButton',
        'shared',
        'title',
        'wide',
      ])
      expect(declaredClassNames(source)).toEqual(
        compiledClassNames(result.output)
      )
    })

    it('compiles an empty source', async () => {
      const result = await compiler.compile({ source: '', path: 'styles.css' })

      expect(result.success).toBe(true)
      expect(compiledClassNames(result.output)).toEqual([])
    })

    it('reports invalid css as an error from css-modules', async () => {
      const source = '.button { "not valid".css\n  color: ;\n}'

      const result = await compiler.compile({ source, path: 'styles.css' })

      expect(result.success).toBe(false)
      expect(result.diagnostics.map((d) => [d.severity, d.source])).toEqual([
        ['error', 'css-modules'],
      ])
    })
  })
})
