import typings from 'virtual:periscope-typings'
import { beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { languageService as language, TokenKind } from '../../src/language'

const utils = `export function add(a: number, b: number) {
  return a + b
}

/** The answer. */
export const ANSWER = 42
`

const styles = `.title-text { color: red; }
.body { background: url(image.png); }
`

async function setProject(main: string) {
  await language.setFiles({
    files: [
      { path: 'shared/utils', kind: 'typescript', source: utils },
      { path: 'shared/styles', kind: 'css', source: styles },
      { path: 'app/main', kind: 'typescript', source: main },
    ],
  })
}

function offsetOf(source: string, marker: string) {
  return source.indexOf(marker) + marker.length
}

async function typeKey(source: string, key: string) {
  await setProject(source)
  const { edits } = await language.formatOnType({
    path: 'app/main',
    offset: source.length,
    key,
  })
  return [...edits]
    .sort((a, b) => b.start - a.start)
    .reduce(
      (text, edit) =>
        text.slice(0, edit.start) +
        edit.newText +
        text.slice(edit.start + edit.length),
      source
    )
}

describe('TypeScript Language Service', () => {
  beforeAll(async () => {
    await language.setTypings(typings)
  })

  beforeEach(async () => {
    await setProject('')
  })

  describe('formatOnType()', () => {
    it('formats the statement a semicolon ends', async () => {
      expect(await typeKey('const  x=1;', ';')).toBe('const x = 1;')
    })

    it('formats the block a closing brace ends', async () => {
      expect(await typeKey('if (true) {\n      run()\n}', '}')).toBe(
        'if (true) {\n  run()\n}'
      )
    })

    it('indents a new line', async () => {
      expect(await typeKey('function f() {\n', '\n')).toBe('function f() {\n  ')
    })
  })

  describe('completions()', () => {
    it('completes exports of other resources with an auto import', async () => {
      const main = 'ad'
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: 'ad',
      })

      const add = entries.find((e) => e.name === 'add')
      expect(add?.source).toBe('@/shared/utils')
      expect(add?.hasAction).toBe(true)

      const details = await language.completionDetails({
        path: 'app/main',
        offset: main.length,
        name: 'add',
        source: add?.source,
        data: add?.data,
      })
      expect(details?.codeActions[0].changes[0].edits[0].newText).toContain(
        "import { add } from '@/shared/utils'"
      )
    })

    it('ranks prefix matches first and details entries without a source', async () => {
      const main = 'ANS'
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: 'ANS',
      })
      expect(entries[0].name).toBe('ANSWER')
      expect(entries.map((e) => e.name)).not.toContain('addEventListener')

      const details = await language.completionDetails({
        path: 'app/main',
        offset: main.length,
        name: 'AnalyserNode',
        source: null as unknown as undefined,
        data: null,
      })
      expect(details?.display).toBeTruthy()
    })

    it('completes camel humps', async () => {
      const main = 'document.aEL'
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: 'aEL',
      })

      expect(entries[0].name).toBe('addEventListener')
    })

    it('completes client resource paths in imports', async () => {
      const main = "import { add } from '@/shared/"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
      })

      expect(entries.map((e) => e.name)).toEqual(
        expect.arrayContaining(['utils', 'styles'])
      )
    })

    it('completes system modules in imports', async () => {
      const main = "import { toast } from 'sys"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
      })

      expect(entries.map((e) => e.name)).toEqual(
        expect.arrayContaining(['system:periscope', 'system:perspective'])
      )
    })

    it('completes only importable paths', async () => {
      const main = "import { add } from ''"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(entries.map((e) => e.name)).toEqual([
        '.',
        '..',
        '@',
        'system:periscope',
        'system:perspective',
      ])
    })

    it('completes the parent of a relative path', async () => {
      const main = "import { add } from '../'"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(entries.map((e) => e.name)).toEqual(['app', 'shared'])
    })

    it('completes going up from a nested resource', async () => {
      const main = "import { add } from './'"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(entries.map((e) => e.name)).toContain('..')
    })

    it('completes only resource folders below the root', async () => {
      const main = "import { add } from '@/'"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(entries.map((e) => e.name)).toEqual(['app', 'shared'])
    })

    it('closes an import path it completes', async () => {
      const main = "import { toast } from 'system:per"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: 'per',
      })

      expect(entries[0]).toMatchObject({
        insertText: "system:periscope'",
        replacementSpan: { start: 23, length: 10 },
      })
    })

    it('leaves a closed import path closed', async () => {
      const main = "import { add } from '@/shared/u'"
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length - 1,
        prefix: 'u',
      })

      expect(entries[0]).toMatchObject({ name: 'utils', insertText: 'utils' })
    })

    it('completes the exports the Gateway serves for each system module', async () => {
      const modules = {
        periscope: ['resource', 'toast'],
        perspective: ['context', 'createView', 'sendMessage'],
      }

      for (const [name, exports] of Object.entries(modules)) {
        const main = `import {  } from 'system:${name}'`
        await setProject(main)
        const { entries } = await language.completions({
          path: 'app/main',
          offset: main.indexOf('{') + 2,
        })
        expect(entries.map((e) => e.name)).toEqual(exports)
      }
    })

    it('never offers to import from a package', async () => {
      const main = 'useMemo'
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: main,
      })

      expect(entries.filter((e) => e.source != null)).toEqual([])
    })

    it('never offers an auto import inside an embedded script', async () => {
      const source = 'function run() {\n  useMemo\n}'
      const action = {
        path: '__embedded__/action',
        kind: 'javascript' as const,
        source,
      }
      await language.updateFile(action)

      const { entries } = await language.completions({
        path: action.path,
        offset: offsetOf(source, 'useMemo'),
        prefix: 'useMemo',
      })

      expect(entries.filter((e) => e.source != null)).toEqual([])
      await language.removeFile({ path: action.path })
    })

    it('completes every global without a prefix', async () => {
      const { entries } = await language.completions({
        path: 'app/main',
        offset: 0,
      })

      expect(entries.length).toBeGreaterThan(250)
      expect(entries.map((e) => e.name)).toContain('WebSocket')
    })
  })

  describe('definition()', () => {
    it('finds definitions in other resources', async () => {
      const main = "import { add } from '@/shared/utils'\nadd(1, 2)\n"
      await setProject(main)

      const result = await language.definition({
        path: 'app/main',
        offset: offsetOf(main, '\nad'),
      })

      expect(result?.definitions[0]).toMatchObject({
        path: 'shared/utils',
        line: 1,
      })
    })

    it('finds the resource an import specifier names', async () => {
      const main = "import { add } from '@/shared/utils'\n"
      await setProject(main)

      const result = await language.definition({
        path: 'app/main',
        offset: offsetOf(main, '@/sha'),
      })

      expect(result?.definitions[0]).toMatchObject({
        path: 'shared/utils',
        start: 0,
      })
    })
  })

  describe('diagnostics()', () => {
    it('reports type errors across resources', async () => {
      const main = "import { add } from '@/shared/utils'\nadd('1', 2)\n"
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })

      expect(diagnostics[0]).toMatchObject({ severity: 'error', code: '2345' })
    })

    it('types React, the DOM and system modules', async () => {
      const main = [
        "import { toast } from 'system:periscope'",
        "import { sendMessage } from 'system:perspective'",
        'export function Hello() {',
        '  const [count, setCount] = React.useState(0)',
        '  toast(document.title)',
        "  sendMessage('hello', { count })",
        '  return <div onClick={() => setCount(count + 1)}>{count}</div>',
        '}',
        '',
      ].join('\n')
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })

      expect(diagnostics.filter((d) => d.severity === 'error')).toEqual([])
    })

    it('documents CSS module members', async () => {
      const main = "import styles from '@/shared/styles'\nstyles.titleText\n"
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: main.length - 2,
      })

      expect(info?.documentationHtml).toContain('title-text')
    })

    it('types CSS module classes', async () => {
      const main =
        "import styles from '@/shared/styles'\nstyles.titleText\nstyles.missing\n"
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })

      expect(diagnostics.map((d) => d.message)).toEqual([
        expect.stringContaining("'missing'"),
      ])
    })

    it('checks JavaScript without reporting type errors', async () => {
      await language.updateFile({
        path: '__embedded__/action',
        kind: 'javascript',
        source: 'const n = 1\nn.toFixed(2)\nn.missing',
      })

      const { diagnostics } = await language.diagnostics({
        path: '__embedded__/action',
      })
      const { entries } = await language.completions({
        path: '__embedded__/action',
        offset: 'const n = 1\nn.to'.length,
        prefix: 'to',
      })
      const { symbols } = await language.symbols({ query: 'n' })

      expect(diagnostics).toEqual([])
      expect(entries.map((e) => e.name)).toContain('toFixed')
      expect(symbols.map((s) => s.location.path)).not.toContain(
        '__embedded__/action'
      )
      await language.removeFile({ path: '__embedded__/action' })
    })

    it('reports syntax errors in embedded scripts and keeps them out of the globals', async () => {
      const action = {
        path: '__embedded__/action',
        kind: 'javascript' as const,
        source: 'function runAction() {\n  const = 1\n}',
      }
      await setProject('runAction()')
      await language.updateFile(action)

      const embedded = await language.diagnostics({ path: action.path })
      const resource = await language.diagnostics({ path: 'app/main' })

      expect(embedded.diagnostics[0].message).toContain(
        'Variable declaration expected'
      )
      expect(resource.diagnostics[0].message).toContain("'runAction'")
      await language.removeFile({ path: action.path })
    })

    it('types an action script parameters as JSDoc declares them', async () => {
      const source = [
        '/** @param {PerspectiveClient.ComponentStore} self @param {{}} event */',
        'async function runAction(self, event) {',
        '  self.',
        '}',
      ].join('\n')
      const action = {
        path: '__embedded__/action',
        kind: 'javascript' as const,
        source,
      }
      await language.updateFile(action)

      const { entries } = await language.completions({
        path: action.path,
        offset: offsetOf(source, 'self.'),
      })
      const info = await language.quickInfo({
        path: action.path,
        offset: offsetOf(source, 'runAction(sel'),
      })

      expect(entries.map((e) => e.name)).toEqual(
        expect.arrayContaining(['props', 'view', 'parent', 'focus'])
      )
      expect(info?.display).toBe('(parameter) self: ComponentStore')
      await language.removeFile({ path: action.path })
    })

    it('types the PerspectiveClient global', async () => {
      const main = [
        'export function pagePath(store: PerspectiveClient.ComponentStore): string {',
        '  return store.view.resourcePath',
        '}',
        'export const scope = PerspectiveClient.ClientScope',
        'PerspectiveClient.',
      ].join('\n')
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })
      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
      })

      expect(diagnostics.filter((d) => d.severity === 'error')).toEqual([])
      expect(entries.map((e) => e.name)).toEqual(
        expect.arrayContaining([
          'ClientStore',
          'ComponentStore',
          'PropertyTree',
        ])
      )
    })

    it('types React in Perspective typings, and reports importing it', async () => {
      const main = [
        "import React from 'react'",
        'declare const store: PerspectiveClient.ComponentStore',
        'export const component: number = store.Component',
      ].join('\n')
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })
      const errors = diagnostics.filter((d) => d.severity === 'error')

      expect(errors.map((d) => d.message)).toEqual([
        "'react' can't be imported in the browser: use the global 'React', or 'import type' for its types.",
        expect.stringContaining("Type 'ComponentClass<{}, any>'"),
      ])
    })

    it('allows type-only imports of packages', async () => {
      const main = [
        "import type { FC } from 'react'",
        "import { type ReactNode } from 'react'",
        'export const Hello: FC<{ children: ReactNode }> = ({ children }) => <b>{children}</b>',
      ].join('\n')
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })

      expect(diagnostics.filter((d) => d.severity === 'error')).toEqual([])
    })

    it('reports packages the browser cannot load, in place of a missing module', async () => {
      const main = [
        "import { chunk } from 'lodash'",
        "export { chunk } from 'lodash'",
        "export const later = import('lodash')",
      ].join('\n')
      await setProject(main)

      const { diagnostics } = await language.diagnostics({ path: 'app/main' })
      const errors = diagnostics.filter((d) => d.severity === 'error')

      expect(errors.map((d) => [d.code, d.range?.start.line])).toEqual([
        ['99001', 1],
        ['99001', 2],
        ['99001', 3],
      ])
    })
  })

  describe('quickInfo()', () => {
    it('shows quick info with documentation', async () => {
      const main = "import { ANSWER } from '@/shared/utils'\nANSWER\n"
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: offsetOf(main, '\nANS'),
      })

      expect(info).toMatchObject({
        display: '(alias) const ANSWER: 42\nimport ANSWER',
        documentationHtml: '<p>The answer.</p>\n',
      })
    })

    it('shows one parameter per line', async () => {
      const main =
        'function run(a: number, done: (error?: Error) => void): void {}\nrun'
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(info?.display).toBe(
        'function run(\n    a: number,\n    done: (error?: Error) => void\n): void'
      )
    })

    it('keeps a signature without parameters on one line', async () => {
      const main = 'function run(): void {}\nrun'
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(info?.display).toBe('function run(): void')
    })

    it('types a loaded resource as an ES module', async () => {
      const main = "import { resource } from 'system:periscope'\nresource"
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(info?.display).toContain('Promise<ESModule>')
    })

    it('types the Perspective client as always present', async () => {
      const main =
        "import { context } from 'system:perspective'\ncontext.client"
      await setProject(main)

      const info = await language.quickInfo({
        path: 'app/main',
        offset: main.length - 1,
      })

      expect(info?.display).toBe('(property) client: ClientStore')
    })
  })

  describe('classifications()', () => {
    it('classifies tokens semantically', async () => {
      const main =
        '// note\nconst value: Map<string, number> = new Map()\nfunction f(p: number) { return p }\n'
      await setProject(main)

      const { tokens } = await language.classifications({ path: 'app/main' })
      const kindAt = (marker: string) => {
        const offset = main.indexOf(marker)
        for (let i = 0; i < tokens.length; i += 3) {
          if (tokens[i] === offset) return tokens[i + 2]
        }
        return undefined
      }

      expect(kindAt('// note')).toBe(TokenKind.LINE_COMMENT)
      expect(kindAt('const')).toBe(TokenKind.KEYWORD)
      expect(kindAt('value')).toBe(TokenKind.CONSTANT)
      expect(kindAt('Map<')).toBe(TokenKind.TYPE)
      expect(kindAt('f(')).toBe(TokenKind.FUNCTION)
      expect(kindAt('p: number')).toBe(TokenKind.PARAMETER)
      expect(kindAt('{ return')).toBe(TokenKind.BRACKET)
    })
  })

  describe('signatureHelp()', () => {
    it('shows signature help for calls', async () => {
      const main = "import { add } from '@/shared/utils'\nadd(1, "
      await setProject(main)

      const help = await language.signatureHelp({
        path: 'app/main',
        offset: main.length,
      })

      expect(help).toMatchObject({
        argumentIndex: 1,
        items: [{ prefix: 'add(', parameters: ['a: number', 'b: number'] }],
      })
    })
  })

  describe('codeFixes()', () => {
    it('never offers an import inside an embedded script', async () => {
      const source = 'function run() {\n  return useMemo\n}'
      const action = {
        path: '__embedded__/action',
        kind: 'javascript' as const,
        source,
      }
      await language.updateFile(action)

      const { actions } = await language.codeFixes({
        path: action.path,
        start: source.indexOf('useMemo'),
        end: offsetOf(source, 'useMemo'),
      })

      expect(actions).toEqual([])
      await language.removeFile({ path: action.path })
    })

    it('offers a missing import as a code fix', async () => {
      const main = 'add(1, 2)\n'
      await setProject(main)

      const { actions } = await language.codeFixes({
        path: 'app/main',
        start: 0,
        end: 3,
      })

      expect(actions[0].description).toBe('Add import from "@/shared/utils"')
    })
  })

  describe('organizeImports()', () => {
    it('organizes imports', async () => {
      const main = "import { ANSWER, add } from '@/shared/utils'\nadd(1, 2)\n"
      await setProject(main)

      const { edits } = await language.organizeImports({ path: 'app/main' })

      expect(edits[0].newText).toBe("import { add } from '@/shared/utils'\n")
    })
  })

  describe('references()', () => {
    it('finds references across resources', async () => {
      const main = "import { add } from '@/shared/utils'\nadd(1, 2)\n"
      await setProject(main)

      const { references } = await language.references({
        path: 'shared/utils',
        offset: offsetOf(utils, 'function a'),
      })

      expect(references.map((r) => r.path)).toEqual(
        expect.arrayContaining(['shared/utils', 'app/main'])
      )
    })
  })

  describe('jsxClosingTag()', () => {
    it('closes JSX tags', async () => {
      const main = 'const view = <div>'
      await setProject(main)

      const tag = await language.jsxClosingTag({
        path: 'app/main',
        offset: main.length,
      })

      expect(tag?.newText).toBe('</div>')
    })
  })

  describe('linkedEditing()', () => {
    it('links the names of a JSX element tags', async () => {
      const main = 'const view = <div>text</div>'
      await setProject(main)

      const { ranges } = await language.linkedEditing({
        path: 'app/main',
        offset: offsetOf(main, '<di'),
      })

      expect(ranges).toEqual([
        { start: main.indexOf('div'), length: 3 },
        { start: main.lastIndexOf('div'), length: 3 },
      ])
    })
  })

  describe('outline()', () => {
    it('outlines a resource without its imports and callbacks', async () => {
      await setProject(
        "import { add } from '@/shared/utils'\nexport const total = add(1, 2)\nvoid [1].map(() => 2)\n"
      )
      const main = await language.outline({ path: 'app/main' })
      const { items } = await language.outline({ path: 'shared/utils' })

      expect(main.items.map((item) => item.text)).toEqual(['total'])
      expect(items.map((item) => [item.text, item.kind, item.detail])).toEqual([
        ['add', 'function', '(a, b)'],
        ['ANSWER', 'const', ''],
      ])
    })

    it('leaves out the members of object literals', async () => {
      await setProject(
        [
          'function make() {',
          '  const local = 1',
          '  return { a: local, b() {} }',
          '}',
          'const arrow = () => ({ c: 1 })',
          'const config = { d: 1 }',
          'class Box { size = 1 }',
        ].join('\n')
      )
      const { items } = await language.outline({ path: 'app/main' })

      const tree = (item: { text: string; children: unknown[] }): unknown => [
        item.text,
        (item.children as typeof items).map(tree),
      ]
      expect(items.map(tree)).toEqual([
        ['arrow', []],
        ['Box', [['size', []]]],
        ['config', []],
        ['make', [['local', []]]],
      ])
    })
  })

  describe('symbols()', () => {
    it('finds symbols across resources', async () => {
      const { symbols } = await language.symbols({ query: 'ANS' })

      expect(symbols[0]).toMatchObject({
        name: 'ANSWER',
        location: { path: 'shared/utils', line: 6, length: 'ANSWER'.length },
      })
    })
  })

  describe('inlayHints()', () => {
    it('provides inlay hints, without arrow functions return types', async () => {
      const main =
        "import { add } from '@/shared/utils'\nconst total = add(1, 2)\nvoid [1].forEach(() => {})\n"
      await setProject(main)

      const { hints } = await language.inlayHints({ path: 'app/main' })

      expect(hints.map((hint) => hint.text)).toEqual([': number', 'a:', 'b:'])
    })
  })
})
