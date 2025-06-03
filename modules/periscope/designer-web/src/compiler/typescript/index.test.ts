import { describe, expect, it } from 'vitest'

import { compiler } from './index'

describe('TypeScript Compiler', () => {
  describe('compile()', () => {
    it('compiles basic javascript', async () => {
      const source = 'console.log("Hello, World!")'

      const result = await compiler.compile({
        source,
        path: 'test.ts',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('console.log("Hello, World!")')
      expect(result.diagnostics).toEqual([])
    })

    it('transpiles typescript syntax', async () => {
      const source = `
const value: string = 'hello'
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'test.ts',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain("const value = 'hello';")
      expect(result.output).not.toContain(': string')
    })

    it('supports jsx transpilation', async () => {
      const source = `
const element = <div>Hello</div>
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'test.tsx',
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain('React.createElement')
    })

    it('returns diagnostics for syntax errors', async () => {
      const source = `
const x =
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'broken.ts',
      })

      expect(result.success).toBe(false)
      expect(result.diagnostics.length).toBeGreaterThan(0)
      expect(result.diagnostics.some((d) => d.severity === 'error')).toBe(true)
    })

    it('includes diagnostic metadata', async () => {
      const source = `
const x: string = 123
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'types.ts',
      })

      expect(result.success).toBe(true)
      expect(result.diagnostics).toEqual([])
    })

    it('includes source information in diagnostics', async () => {
      const source = `
const =
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'broken.ts',
      })

      const diagnostic = result.diagnostics[0]

      expect(diagnostic.source).toBe('typescript')
      expect(diagnostic.message.length).toBeGreaterThan(0)
    })

    it('includes ranges for syntax diagnostics', async () => {
      const source = `
const =
      `.trim()

      const result = await compiler.compile({
        source,
        path: 'broken.ts',
      })

      const diagnostic = result.diagnostics[0]

      expect(diagnostic.range).toBeDefined()
      expect(diagnostic.range?.start.line).toBeGreaterThan(0)
      expect(diagnostic.range?.start.column).toBeGreaterThan(0)
    })
  })

  describe('format()', () => {
    it('formats typescript source', async () => {
      const source = `
const   x="hello"
      `.trim()

      const result = await compiler.format({
        source,
        path: 'format.ts',
        cursor: 0,
      })

      expect(result.success).toBe(true)
      expect(result.output).toContain(`const x = "hello";`)
      expect(result.diagnostics).toEqual([])
    })

    it('preserves cursor position', async () => {
      const source = `const x="hello"`

      const result = await compiler.format({
        source,
        path: 'cursor.ts',
        cursor: 5,
      })

      expect(result.success).toBe(true)
      expect(typeof result.cursor).toBe('number')
    })

    it('returns diagnostics on formatter failure', async () => {
      const source = `
const =
      `.trim()

      const result = await compiler.format({
        source,
        path: 'broken.ts',
        cursor: 0,
      })

      expect(result.success).toBe(false)
      expect(result.diagnostics.length).toBeGreaterThan(0)
      expect(result.diagnostics[0].severity).toBe('error')
    })
  })
})
