import { describe, expect, it } from 'vitest'

import { compiler } from './index'

describe('CSS Module Compiler', () => {
  describe('compile()', () => {
    it('compiles basic css modules', async () => {
      const source = `
.button {
  color: red;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'styles.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('export const cssText')
      expect(result.output).toContain('export const styles')
      expect(result.output).toContain('CSSStyleSheet')
      expect(result.diagnostics).toEqual([])
    })

    it('exports generated class mappings', async () => {
      const source = `
.button {
  color: red;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'styles.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('button')
    })

    it('supports dashed class names', async () => {
      const source = `
.my-button {
  color: red;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'styles.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('my-button')
    })

    it('injects stylesheet helper', async () => {
      const source = `
.container {
  display: flex;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'layout.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('applyStyle')
      expect(result.output).toContain('adoptedStyleSheets')
    })

    it('preserves css content', async () => {
      const source = `
.button {
  background: blue;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'button.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('background: blue')
    })

    it('returns diagnostics for invalid css', async () => {
      const source = `
.button { "not valid".css
  color: ;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'broken.module.css',
      })

      expect(result.success).toBe(false)
      expect(result.diagnostics.length).toBeGreaterThan(0)
      expect(result.diagnostics.some((d) => d.severity === 'error')).toBe(true)
    })

    it('includes diagnostic source information', async () => {
      const source = `
.button { "not valid".css
  color:
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'broken.module.css',
      })

      const diagnostic = result.diagnostics[0]

      expect(diagnostic.source).toBe('css-modules')
      expect(diagnostic.message.length).toBeGreaterThan(0)
    })

    it('handles empty files', async () => {
      const source = ''

      const result = await compiler.compile({
        source,
        path: 'empty.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('export const cssText')
    })

    it('supports multiple classes', async () => {
      const source = `
.button {
  color: red;
}

.container {
  display: flex;
}

.title {
  font-size: 20px;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'multi.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('button')
      expect(result.output).toContain('container')
      expect(result.output).toContain('title')
    })

    it('supports nested selectors', async () => {
      const source = `
.container .button {
  color: red;
}
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'nested.module.css',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('color: red')
    })

    it('handles CRLF line endings', async () => {
      const source = '.button {\r\n  color: red;\r\n}'

      const result = await compiler.compile({
        source,
        path: 'windows.module.css',
      })

      expect(result.success).toBe(true)
    })
  })
})
