import { describe, expect, it } from 'vitest'
import { languageService as language } from '../../../src/typescript/language'
import {
  ComponentsResult,
  ResourceKind,
} from '../../../src/typescript/language/protocol'

const shared = `import * as React from 'react'
export const Theme = React.createContext('light')
export const kinds = ['pump', 'valve'] as const
`

const cards = `import * as React from 'react'
import { createPortal } from 'react-dom'
import { defineComponent, event, prop } from 'system:periscope'
import { context } from 'system:perspective'
import styles from '@/equipment/styles'
import { kinds, Theme } from '@/equipment/shared'

const session = context.client.projectName

export const cardFor = (kind: (typeof kinds)[number]) =>
  defineComponent({
    description: \`A \${kind} card\`,
    props: { title: prop.string().default(kind), count: prop.number().int().min(0).default(0) },
    events: { onPick: event({ id: prop.string() }).describe('Picked') },
    position: { slot: prop.enum(['header', 'body']).default('body') },
    variants: [{ name: 'Busy', props: { count: 9 } }],
    render: ({ title }, { Root }) => (
      <Root className={styles.card}>
        {React.useContext(Theme)}
        {createPortal(title, document.body)}
        {String(session)}
      </Root>
    ),
  })
`

const pump = `import { cardFor } from '@/equipment/cards'
export default cardFor('pump')
`

const valve = `import { cardFor } from './cards'
export default cardFor('valve')
`

const broken = `import { defineComponent } from 'system:periscope'
const missing: { value: { text: string } } = undefined as never
export default defineComponent({ description: missing.value.text, render: () => null })
`

const kinds: Record<string, ResourceKind> = {
  'equipment/styles': 'css',
  'equipment/pump': 'component',
  'equipment/valve': 'component',
  'equipment/broken': 'component',
  'equipment/plain': 'component',
  'equipment/other': 'component',
}

const project = {
  'equipment/shared': shared,
  'equipment/styles': '.card { color: red; }',
  'equipment/cards': cards,
  'equipment/pump': pump,
  'equipment/valve': valve,
}

function createResource(path: string, source: string) {
  return { path, kind: kinds[path] ?? 'typescript', source }
}

async function setProject(files: Record<string, string>) {
  await language.setFiles({
    files: Object.entries(files).map(([path, source]) =>
      createResource(path, source)
    ),
  })
}

async function updateResource(path: string, source: string) {
  await language.updateFile(createResource(path, source))
}

async function describeAll() {
  const result = await language.describeComponents({})
  return result as ComponentsResult
}

function getPaths({ components }: ComponentsResult) {
  return components.map((component) => component.path)
}

describe('Components', () => {
  describe('describeComponents()', () => {
    it('describes the component each Component resource exports', async () => {
      await setProject(project)

      const result = await describeAll()

      expect(result.failures).toEqual([])
      expect(getPaths(result)).toEqual(['equipment/pump', 'equipment/valve'])
      expect(result.components[0]).toMatchObject({
        path: 'equipment/pump',
        description: 'A pump card',
        source: 'equipment/pump',
        defaults: { title: 'pump', count: 0, style: { classes: '' } },
        events: [{ name: 'onPick', description: 'Picked' }],
        position: { default: { slot: 'body' } },
        variants: [{ name: 'Busy', props: { title: 'pump', count: 9 } }],
        schema: {
          properties: { count: { type: 'integer', minimum: 0, default: 0 } },
        },
      })
    })

    it('describes nothing when the project has no Component resource', async () => {
      await setProject({
        'equipment/shared': shared,
        'equipment/styles': '.card { color: red; }',
        'equipment/cards': cards,
      })

      const result = await describeAll()

      expect(result.failures).toEqual([])
      expect(result.components).toEqual([])
    })

    it('keeps the components that load when another fails', async () => {
      await setProject({ ...project, 'equipment/broken': broken })

      const result = await describeAll()

      expect(result.components).toHaveLength(2)
      expect(result.failures).toEqual([
        { path: 'equipment/broken', message: expect.stringContaining('value') },
      ])
    })

    it('reports a Component resource whose default export is not made with defineComponent', async () => {
      await setProject({
        ...project,
        'equipment/plain': `export default () => null
`,
      })

      const result = await describeAll()

      expect(result.components).toHaveLength(2)
      expect(result.failures).toEqual([
        {
          path: 'equipment/plain',
          message: expect.stringContaining('must be made with defineComponent'),
        },
      ])
      expect(await language.diagnostics({ path: 'equipment/plain' })).toEqual({
        diagnostics: [
          {
            severity: 'error',
            message: expect.stringContaining(
              'must be made with defineComponent'
            ),
          },
        ],
      })
    })

    it('evaluates again only after a resource changes', async () => {
      await setProject(project)
      const first = await describeAll()
      const second = await describeAll()

      await updateResource(
        'equipment/shared',
        `${shared}\nexport const more = 1\n`
      )
      const third = await describeAll()

      expect(second).toBe(first)
      expect(third).not.toBe(first)
    })

    it('answers with only the revision while the caller is up to date', async () => {
      await setProject(project)
      const { revision } = await describeAll()

      expect(await language.describeComponents({ revision })).toEqual({
        revision,
      })

      await updateResource(
        'equipment/shared',
        `${shared}\nexport const other = 2\n`
      )
      const changed = await language.describeComponents({ revision })

      expect(changed).toMatchObject({ revision: revision + 1 })
      expect(changed).toHaveProperty('components')
    })

    it('keeps its answer when a resource no component imports changes', async () => {
      await setProject({ ...project, 'notes/unused': 'export const note = 1' })
      const first = await describeAll()

      await updateResource('notes/unused', 'export const note = 2')

      expect(await describeAll()).toBe(first)
    })

    it('runs again only the resources a change reaches', async () => {
      const tally = globalThis as unknown as { runs: Record<string, number> }
      const counted = (name: string, source: string) =>
        [
          `const runs = (globalThis as any).runs`,
          `runs['${name}'] = (runs['${name}'] ?? 0) + 1`,
          source,
        ].join(';')
      const other = [
        `import { defineComponent } from 'system:periscope'`,
        `export default defineComponent({ render: () => null })`,
      ].join(';')
      tally.runs = {}
      await setProject({
        ...project,
        'equipment/shared': counted('shared', shared),
        'equipment/cards': counted('cards', cards),
        'equipment/other': counted('other', other),
      })
      await describeAll()
      expect({ ...tally.runs }).toEqual({ shared: 1, cards: 1, other: 1 })

      await updateResource(
        'equipment/shared',
        counted('shared', `${shared};export const more = 3`)
      )
      const result = await describeAll()

      expect({ ...tally.runs }).toEqual({ shared: 2, cards: 2, other: 1 })
      expect(getPaths(result)).toEqual([
        'equipment/other',
        'equipment/pump',
        'equipment/valve',
      ])
    })
  })
})
