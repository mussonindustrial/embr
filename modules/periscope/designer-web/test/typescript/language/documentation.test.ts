import ts from 'typescript'
import { describe, expect, it } from 'vitest'
import { documentationHtml } from '../../../src/typescript/language/documentation'

function text(text: string): ts.SymbolDisplayPart[] {
  return [{ text, kind: 'text' }]
}

describe('Documentation', () => {
  describe('documentationHtml()', () => {
    it('renders markdown', async () => {
      const html = documentationHtml(
        text('Shows **bold** and `code`.\n\n```ts\nrun()\n```')
      )

      expect(html).toBe(
        '<p>Shows <strong>bold</strong> and <code>code</code>.</p>\n' +
          '<pre><code class="language-ts">run()\n</code></pre>\n'
      )
    })

    it('escapes html in documentation', async () => {
      expect(documentationHtml(text('A <div> tag.'))).toBe(
        '<p>A &lt;div&gt; tag.</p>\n'
      )
    })

    it('lists parameters and the return value', async () => {
      const html = documentationHtml(text('Adds.'), [
        {
          name: 'param',
          text: [
            { text: 'a', kind: 'parameterName' },
            { text: ' ', kind: 'space' },
            { text: '- The **first** number.', kind: 'text' },
          ],
        },
        { name: 'returns', text: text('The sum.') },
      ])

      expect(html).toBe(
        '<p>Adds.</p>\n' +
          '<p><b>Parameters</b></p><ul><li><b>a</b> The <strong>first</strong> number.</li></ul>' +
          '<p><b>Returns: </b>The sum.</p>'
      )
    })

    it('shows links as code', async () => {
      const html = documentationHtml([
        { text: 'See ', kind: 'text' },
        { text: '{@link ', kind: 'link' },
        { text: 'Array', kind: 'linkName' },
        { text: '}', kind: 'link' },
      ])

      expect(html).toBe('<p>See <code>Array</code></p>\n')
    })
  })
})
