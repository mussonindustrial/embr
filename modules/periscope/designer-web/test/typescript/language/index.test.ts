import typings from 'virtual:periscope-typings'
import { beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { languageService as language } from '../../../src/typescript/language'

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

async function errorsOf(main: string) {
  await setProject(main)
  const { diagnostics } = await language.diagnostics({ path: 'app/main' })
  return diagnostics.filter((d) => d.severity === 'error')
}

async function importPathsAfter(typed: string) {
  const main = `import { add } from '${typed}`
  await setProject(main)
  const { entries } = await language.completions({
    path: 'app/main',
    offset: main.length,
  })
  return entries.map((e) => e.name).sort()
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
    await language.warmUp()
  }, 60_000)

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

    it('auto imports from folders named like the typings folders', async () => {
      const folders = ['globals', 'system', 'declarations', 'typings']
      await language.setFiles({
        files: [
          { path: 'app/main', kind: 'typescript', source: 'twice' },
          ...folders.map((folder) => ({
            path: `${folder}/helper`,
            kind: 'typescript' as const,
            source: `export const twice_${folder} = 2\n`,
          })),
        ],
      })

      const { entries } = await language.completions({
        path: 'app/main',
        offset: 5,
        prefix: 'twice',
      })

      expect(entries.map((e) => e.source).sort()).toEqual(
        folders.map((folder) => `@/${folder}/helper`).sort()
      )
    })

    it('ranks names starting with the prefix above camel hump matches', async () => {
      const main = 'ANS'
      await setProject(main)

      const { entries } = await language.completions({
        path: 'app/main',
        offset: main.length,
        prefix: 'ANS',
      })

      expect(entries[0].name).toBe('ANSWER')
    })

    it('details an entry the Designer sends back without a source', async () => {
      const details = await language.completionDetails({
        path: 'app/main',
        offset: 0,
        name: 'AnalyserNode',
        source: null as unknown as undefined,
        data: null,
      })

      expect(details?.display).toContain('AnalyserNode')
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

    it.each([
      [
        '',
        [
          '.',
          '..',
          '@',
          '@inductiveautomation/perspective-client',
          'mobx',
          'mobx-react-lite',
          'react',
          'react-dom',
          'system:periscope',
          'system:perspective',
        ],
      ],
      ['@/', ['app', 'shared']],
      ['@/shared/', ['styles', 'utils']],
      ['../', ['app', 'shared']],
    ])("completes the importable paths after '%s", async (typed, paths) => {
      expect(await importPathsAfter(typed)).toEqual([...paths].sort())
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
        replacementSpan: {
          start: main.indexOf("'") + 1,
          length: 'system:per'.length,
        },
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

    it('completes the exports and types of each system module', async () => {
      const modules = {
        periscope: [
          'ReactComponent',
          'ReactComponentContext',
          'ReactComponentProps',
          'resource',
          'toast',
        ],
        perspective: [
          'context',
          'createView',
          'PerspectiveApiContext',
          'sendMessage',
        ],
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

    it('auto imports from the global modules', async () => {
      const imports: Record<string, string> = {
        useMemo: 'react',
        createPortal: 'react-dom',
        autorun: 'mobx',
        observer: 'mobx-react-lite',
        ClientStore: '@inductiveautomation/perspective-client',
      }

      for (const [name, source] of Object.entries(imports)) {
        await setProject(name)
        const { entries } = await language.completions({
          path: 'app/main',
          offset: name.length,
          prefix: name,
        })
        const entry = entries.find((e) => e.name === name)
        const details = await language.completionDetails({
          path: 'app/main',
          offset: name.length,
          name,
          source: entry?.source,
          data: entry?.data,
        })

        expect(entry?.source).toBe(source)
        expect(details?.codeActions[0].changes[0].edits[0].newText).toContain(
          `import { ${name} } from '${source}'`
        )
      }
    })

    it('never offers to import from another package or a file inside one', async () => {
      await setProject('')

      const { entries } = await language.completions({
        path: 'app/main',
        offset: 0,
      })
      const sources = new Set(entries.map((e) => e.source).filter(Boolean))

      expect([...sources].sort()).toEqual([
        '@/shared/styles',
        '@/shared/utils',
        '@inductiveautomation/perspective-client',
        'mobx',
        'mobx-react-lite',
        'react',
        'react-dom',
      ])
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
      const errors = await errorsOf(
        "import { add } from '@/shared/utils'\nadd('1', 2)\n"
      )

      expect(errors.map((d) => d.code)).toEqual(['2345'])
    })

    it('rechecks a resource when one it imports is edited or removed', async () => {
      const main = "import { add } from '@/shared/utils'\nadd(1, 2)\n"
      const utilsWith = (source: string) => ({
        path: 'shared/utils',
        kind: 'typescript' as const,
        source,
      })
      const codes = async () =>
        (await language.diagnostics({ path: 'app/main' })).diagnostics.map(
          (d) => d.code
        )
      await setProject(main)

      await language.updateFile(
        utilsWith('export const add = (a: string) => a')
      )
      const afterEdit = await codes()
      await language.updateFile(utilsWith(utils))
      const afterRevert = await codes()
      await language.removeFile({ path: 'shared/utils' })
      const afterRemoval = await codes()

      expect({ afterEdit, afterRevert, afterRemoval }).toEqual({
        afterEdit: ['2554'],
        afterRevert: [],
        afterRemoval: ['2307'],
      })
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

      expect(await errorsOf(main)).toEqual([])
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

      expect(embedded.diagnostics[0].code).toBe('1134')
      expect(resource.diagnostics[0].message).toContain("'runAction'")
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

      expect(entries.map((e) => e.name)).toContain('view')
      expect(info?.display).toContain('self: ComponentStore')
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

    it('types a component for the React component, with its props and this', async () => {
      const main = [
        "import type { ReactComponent } from 'system:periscope'",
        'export const View: ReactComponent<{ label: string }> = function ({',
        '  label,',
        '  writeProps,',
        '  renderContext,',
        '}) {',
        "  writeProps('label', label.toUpperCase())",
        '  return this.store === renderContext.store ? label : this.props.resource.path',
        '}',
        'export const Wrong: ReactComponent<{ label: string }> = ({ missing }) => missing',
      ].join('\n')

      const errors = await errorsOf(main)

      expect(errors.map((error) => error.message)).toEqual([
        expect.stringContaining("Property 'missing' does not exist"),
      ])
    })

    it("types the mobx globals, and what Perspective's typings take from mobx and React", async () => {
      const main = [
        'declare const store: PerspectiveClient.ComponentStore',
        'export const Views = mobxReactLite.observer(() => <b>{store.view.page.views.size}</b>)',
        'export const dispose: mobx.IReactionDisposer = mobx.reaction(',
        '  () => store.view.page.views.size,',
        '  () => {}',
        ')',
        '// @ts-expect-error: an ObservableMap, where it would be any without mobx',
        'export const views: string = store.view.page.views',
        '// @ts-expect-error: a React component class, where it would be any without React',
        'export const component: number = store.Component',
      ].join('\n')

      expect(await errorsOf(main)).toEqual([])
    })

    it('imports the global modules', async () => {
      const main = [
        "import React, { useState } from 'react'",
        "import { createPortal } from 'react-dom'",
        "import { reaction } from 'mobx'",
        "import { observer } from 'mobx-react-lite'",
        "import { ClientScope } from '@inductiveautomation/perspective-client'",
        'export const scope = ClientScope',
        'export const Count = observer(() => {',
        '  const [count] = useState(0)',
        '  return createPortal(<b>{count}</b>, document.body)',
        '})',
        'export const dispose = reaction(',
        '  () => React.version,',
        '  () => {}',
        ')',
      ].join('\n')

      expect(await errorsOf(main)).toEqual([])
    })

    it('allows type-only imports of packages the browser cannot load', async () => {
      const main = [
        "import type { Properties } from 'csstype'",
        "import { type Property } from 'csstype'",
        "export const style: Properties = { color: 'red' as Property.Color }",
      ].join('\n')

      expect(await errorsOf(main)).toEqual([])
    })

    it('allows imports the editor has no typings for, and reports a missing resource', async () => {
      const main = [
        "import handler from '@webdev/project/handler'",
        "import confetti from 'https://example.com/confetti.js'",
        "import { missing } from '@/shared/missing'",
        'export const used = [handler, confetti, missing]',
      ].join('\n')

      const errors = await errorsOf(main)

      expect(errors.map((d) => [d.code, d.range?.start.line])).toEqual([
        ['2307', 3],
      ])
    })

    it('reports packages the browser cannot load, in place of a missing module', async () => {
      const main = [
        "import { chunk } from 'lodash'",
        "export { chunk } from 'lodash'",
        "export const later = import('lodash')",
      ].join('\n')

      const errors = await errorsOf(main)

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

      const { kinds, tokens } = await language.classifications({
        path: 'app/main',
      })
      const kindAt = (marker: string) => {
        const offset = main.indexOf(marker)
        for (let i = 0; i < tokens.length; i += 3) {
          if (tokens[i] === offset) return kinds[tokens[i + 2]]
        }
        return undefined
      }

      expect(kindAt('// note')).toBe('LINE_COMMENT')
      expect(kindAt('const')).toBe('KEYWORD')
      expect(kindAt('value')).toBe('CONSTANT')
      expect(kindAt('Map<')).toBe('TYPE')
      expect(kindAt('f(')).toBe('FUNCTION')
      expect(kindAt('p: number')).toBe('PARAMETER')
      expect(kindAt('{ return')).toBe('BRACKET')
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

    it('has no items outside a call', async () => {
      await setProject('const a = 1\n')

      const help = await language.signatureHelp({ path: 'app/main', offset: 0 })

      expect(help.items).toEqual([])
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

  describe('reactComponentNames()', () => {
    it('names the exports typed as a ReactComponent', async () => {
      await setProject(
        [
          "import type { ReactComponent } from 'system:periscope'",
          "export { add } from '@/shared/utils'",
          'type Local<P> = (props: P) => null',
          'export const Gauge: ReactComponent<{ value: number }> = () => null',
          'export const Untyped = () => null',
          'export const Lookalike: Local<object> = () => null',
          'export type Size = number',
          'const Main: ReactComponent = function () {',
          '  return null',
          '}',
          'export default Main',
        ].join('\n')
      )

      const main = await language.reactComponentNames({ path: 'app/main' })
      const missing = await language.reactComponentNames({
        path: 'app/missing',
      })

      expect(main.names.sort()).toEqual(['Gauge', 'default'])
      expect(missing.names).toEqual([])
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
