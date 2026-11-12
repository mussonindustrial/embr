import { describe, expect, it } from 'vitest'

import { compiler } from '../../src/typescript/compiler'

describe('TypeScript Compiler', () => {
  describe('compile()', () => {
    it('compiles TypeScript and JSX to JavaScript', async () => {
      const source = [
        "const value: string = 'hello'",
        'const element = <b>{value}</b>',
      ].join('\n')

      const result = await compiler.compile({ source, path: 'test.tsx' })

      expect(result.success).toBe(true)
      expect(result.diagnostics).toEqual([])
      expect(result.output).toContain("const value = 'hello';")
      expect(result.output).toContain('React.createElement("b", null, value)')
    })

    it('leaves imports for the gateway to resolve', async () => {
      const source = [
        "import React, { useState, type FC } from 'react'",
        "import { add } from '@/shared/utils'",
        'export const View: FC = () => <b>{useState(add(1, 2))[0]}</b>',
      ].join('\n')

      const result = await compiler.compile({ source, path: 'test.tsx' })

      expect(result.success).toBe(true)
      expect(result.output?.split('\n').slice(0, 2)).toEqual([
        "import React, { useState } from 'react';",
        "import { add } from '@/shared/utils';",
      ])
    })
    it('compiles despite type errors, which the editor reports', async () => {
      const source = 'const x: string = 123'

      const result = await compiler.compile({ source, path: 'test.ts' })

      expect(result.success).toBe(true)
      expect(result.diagnostics).toEqual([])
    })

    it('reports a syntax error where it is', async () => {
      const source = 'const ok = 1\nconst ='

      const result = await compiler.compile({ source, path: 'test.ts' })

      expect(result.success).toBe(false)
      expect(result.diagnostics).toMatchObject([
        {
          severity: 'error',
          source: 'typescript',
          range: { start: { line: 2, column: 7 } },
        },
      ])
    })
  })

  describe('format()', () => {
    it('formats the source and moves the cursor with it', async () => {
      const source = 'const   x="hello"'

      const result = await compiler.format({
        source,
        path: 'test.ts',
        cursor: source.indexOf('x') + 1,
      })

      expect(result).toMatchObject({
        success: true,
        output: 'const x = "hello";\n',
        cursor: 'const x'.length,
        diagnostics: [],
      })
    })

    it('returns the source as it was when it cannot be formatted', async () => {
      const source = 'const ='

      const result = await compiler.format({
        source,
        path: 'test.ts',
        cursor: 0,
      })

      expect(result).toMatchObject({
        success: false,
        output: source,
        diagnostics: [{ severity: 'error', source: 'prettier' }],
      })
    })
  })
})
