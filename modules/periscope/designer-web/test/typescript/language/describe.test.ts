import { describe, expect, it } from 'vitest'
import { compile } from '../../../src/typescript/compiler'
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

export const cards = kinds.map((kind) =>
  defineComponent({
    id: \`\${kind}-card\`,
    name: \`\${kind} card\`,
    category: 'Equipment',
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
)

export const Unlisted = defineComponent({ id: 'unlisted', render: () => null })
`

const library = `import { defineComponentLibrary } from 'system:periscope'
import { cards } from '@/equipment/cards'
export default defineComponentLibrary({ namespace: 'plant', components: cards })
`

const broken = `import { defineComponent, defineComponentLibrary } from 'system:periscope'
const missing: { value: { id: string } } = undefined as never
export default defineComponentLibrary({
  namespace: 'broken',
  components: [defineComponent({ id: missing.value.id, render: () => null })],
})
`

const kinds: Record<string, ResourceKind> = {
  'equipment/styles': 'css',
  'equipment/library': 'library',
  'equipment/broken': 'library',
  'equipment/copy': 'library',
  'equipment/single': 'library',
}

const project = {
  'equipment/shared': shared,
  'equipment/styles': '.card { color: red; }',
  'equipment/cards': cards,
  'equipment/library': library,
}

function resourceOf(path: string, source: string) {
  return { path, kind: kinds[path] ?? 'typescript', source }
}

async function setProject(files: Record<string, string>) {
  await language.setFiles({
    files: Object.entries(files).map(([path, source]) =>
      resourceOf(path, source)
    ),
  })
}

async function updateResource(path: string, source: string) {
  await language.updateFile(resourceOf(path, source))
}

function idsOf({ components }: ComponentsResult) {
  return components.map((component) => component.id)
}

describe('describeComponents()', () => {
  it('describes the components a library lists', async () => {
    await setProject(project)

    const result = await language.describeComponents()

    expect(result.failures).toEqual([])
    expect(result.components).toHaveLength(2)
    expect(result.components[0]).toMatchObject({
      id: 'plant.pump-card',
      name: 'pump card',
      category: 'Equipment',
      source: 'equipment/cards',
      defaults: { title: 'pump', count: 0, style: { classes: '' } },
      events: [{ name: 'onPick', description: 'Picked' }],
      position: { default: { slot: 'body' } },
      variants: [{ name: 'Busy', props: { title: 'pump', count: 9 } }],
      schema: {
        properties: { count: { type: 'integer', minimum: 0, default: 0 } },
      },
    })
  })

  it('describes nothing when no library lists a component', async () => {
    await setProject({
      ...project,
      'equipment/library': `import { defineComponentLibrary } from 'system:periscope'
export default defineComponentLibrary({ namespace: 'plant', components: [] })`,
    })

    const result = await language.describeComponents()

    expect(result.failures).toEqual([])
    expect(result.components).toEqual([])
  })

  it('keeps the components that load when another library fails', async () => {
    await setProject({ ...project, 'equipment/broken': broken })

    const result = await language.describeComponents()

    expect(result.components).toHaveLength(2)
    expect(result.failures).toEqual([
      { path: 'equipment/broken', message: expect.stringContaining('value') },
    ])
  })

  it('reports a library whose default export is not made with defineComponentLibrary', async () => {
    await setProject({
      ...project,
      'equipment/single': `import { Unlisted } from '@/equipment/cards'
export default [Unlisted]
`,
    })

    const result = await language.describeComponents()

    expect(result.components).toHaveLength(2)
    expect(result.failures).toEqual([
      {
        path: 'equipment/single',
        message: expect.stringContaining(
          'must be made with defineComponentLibrary'
        ),
      },
    ])
    expect(await language.diagnostics({ path: 'equipment/single' })).toEqual({
      diagnostics: [
        {
          severity: 'error',
          message: expect.stringContaining(
            'must be made with defineComponentLibrary'
          ),
        },
      ],
    })
  })

  it('accepts a component that two libraries list', async () => {
    await setProject({ ...project, 'equipment/copy': library })

    const result = await language.describeComponents()

    expect(result.components).toHaveLength(2)
    expect(result.components[0]).toMatchObject({ diagnostics: [] })
  })

  it('reports a library with an invalid namespace and describes none of its components', async () => {
    await setProject({
      ...project,
      'equipment/library': library.replace("'plant'", "'plant.v2'"),
    })

    const result = await language.describeComponents()

    expect(result.components).toEqual([])
    expect(result.failures).toEqual([
      {
        path: 'equipment/library',
        message: expect.stringContaining('namespace must start with a letter'),
      },
    ])
  })

  it('keeps components with the same id apart when their namespaces differ', async () => {
    await setProject({
      ...project,
      'equipment/copy': library.replace("'plant'", "'spare'"),
    })

    const result = await language.describeComponents()

    expect(idsOf(result)).toEqual([
      'spare.pump-card',
      'spare.valve-card',
      'plant.pump-card',
      'plant.valve-card',
    ])
  })

  it('evaluates again only after a resource changes', async () => {
    await setProject(project)
    const first = await language.describeComponents()
    const second = await language.describeComponents()

    await updateResource(
      'equipment/shared',
      `${shared}\nexport const more = 1\n`
    )
    const third = await language.describeComponents()

    expect(second).toBe(first)
    expect(third).not.toBe(first)
  })

  it('answers with only the revision while the caller is up to date', async () => {
    await setProject(project)
    const { revision } = await language.describeComponents()

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

  it('keeps its answer when a resource no library imports changes', async () => {
    await setProject({ ...project, 'notes/unused': 'export const note = 1' })
    const first = await language.describeComponents()

    await updateResource('notes/unused', 'export const note = 2')

    expect(await language.describeComponents()).toBe(first)
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
      `export const Other = defineComponent({ id: 'other', render: () => null })`,
    ].join(';')
    const single = [
      `import { defineComponentLibrary } from 'system:periscope'`,
      `import { Other } from '@/equipment/other'`,
      `export default defineComponentLibrary({ namespace: 'other', components: [Other] })`,
    ].join(';')
    tally.runs = {}
    await setProject({
      ...project,
      'equipment/shared': counted('shared', shared),
      'equipment/cards': counted('cards', cards),
      'equipment/other': counted('other', other),
      'equipment/single': single,
    })
    await language.describeComponents()
    expect({ ...tally.runs }).toEqual({ shared: 1, cards: 1, other: 1 })

    await updateResource(
      'equipment/shared',
      counted('shared', `${shared};export const more = 3`)
    )
    const result = await language.describeComponents()

    expect({ ...tally.runs }).toEqual({ shared: 2, cards: 2, other: 1 })
    expect(idsOf(result)).toEqual([
      'plant.pump-card',
      'plant.valve-card',
      'other.other',
    ])
  })
})

describe('diagnostics()', () => {
  it('reports what failed in the last evaluation without running one', async () => {
    await setProject({ ...project, 'equipment/broken': broken })
    const before = await language.describeComponents()

    await updateResource('equipment/broken', `${broken};export const more = 4`)
    const { diagnostics } = await language.diagnostics({
      path: 'equipment/broken',
    })

    expect(
      diagnostics.some((diagnostic) => diagnostic.message.includes('undefined'))
    ).toBe(true)
    expect(await language.describeComponents()).not.toBe(before)
  })
})

describe('compile()', () => {
  it('compiles a resource that defines components to what was written', async () => {
    const { success, output } = await compile({
      source: cards,
      path: 'equipment/cards.tsx',
    })

    expect(success).toBe(true)
    expect(output).toContain('defineComponent({')
    expect(output).toContain('export const cards = kinds.map(')
  })
})
