import typings from 'virtual:periscope-typings'
import { beforeAll, describe, expect, it } from 'vitest'
import { languageService as language } from '../../../src/typescript/language'

const card = `import * as React from 'react'
import { defineComponent, event, prop } from 'system:periscope'

const option = prop.object({
  label: prop.string(),
  tone: prop.enum(['info', 'danger']),
})

for (const kind of ['pump', 'valve'] as const) {
  defineComponent({
    props: {
      title: prop.string().default(kind),
      variant: prop.enum(['outlined', 'filled']).default('outlined'),
      count: prop.number().int().min(0).default(0),
      options: prop.array(option).default([{ label: 'A', tone: 'info' }]),
      rows: prop.custom<{ id: number }[]>().default([]),
    },
    events: {
      onPick: event({ kind: prop.string(), count: prop.number() }),
      onReset: event().describe('The count was reset.'),
    },
    render: ({ title, variant, options, rows, onPick, onReset }, ctx) => {
      const [count, setCount] = ctx.useProp('count')
      const [label, setLabel] = ctx.useProp<string>('options[0].label')
      const danger = options.filter((item) => item.tone === 'danger')

      return (
        <ctx.Root as="section" className={variant} data-mode={ctx.mode}>
          <b>{title.toUpperCase()}</b>
          <button onClick={() => setCount(count + 1)}>{count}</button>
          <button onClick={() => onPick({ kind, count })}>pick</button>
          <button onClick={() => onReset()}>reset</button>
          <input value={label} onChange={(e) => setLabel(e.target.value)} />
          <i>{danger.length + rows.length}</i>
        </ctx.Root>
      )
    },
  })
}
`

const panel = `import * as React from 'react'
import { defineComponent, prop, useComponent } from 'system:periscope'

function Count() {
  const { children } = useComponent()
  return <i>{children.length}</i>
}

defineComponent({
  props: { title: prop.string() },
  position: {
    slot: prop.enum(['header', 'content']).default('content'),
  },
  render: ({ title }, ctx) => (
    <ctx.Root>
      <header>
        {title}
        {ctx.children
          .filter((child) => child.position.slot === 'header')
          .map((child) => child.render({ className: child.name }))}
      </header>
      <main>
        {ctx.children.map((child) => child.render())}
        <Count />
      </main>
    </ctx.Root>
  ),
})
`

const slots = `import * as React from 'react'
import { defineComponent, prop } from 'system:periscope'

defineComponent({
  props: { title: prop.string() },
  position: {
    slot: prop.enum(['header', 'content']).default('content'),
  },
  render: ({ title }, { Root, ChildSlot }) => (
    <Root>
      <ChildSlot
        as="header"
        where={{ slot: 'header' }}
        placeholder={title}
      />
      <ChildSlot
        as="ul"
        where={{ slot: 'content' }}
      >
        {(child) => <li>{child.render({ className: child.position.slot })}</li>}
      </ChildSlot>
    </Root>
  ),
})
`

async function findErrors(source: string) {
  await language.setFiles({
    files: [{ path: 'equipment/card', kind: 'typescript', source }],
  })
  const { diagnostics } = await language.diagnostics({ path: 'equipment/card' })
  return diagnostics
    .filter((d) => d.severity === 'error')
    .map((d) => d.message.split('\n')[0])
}

describe('React Components', () => {
  beforeAll(async () => {
    await language.setTypings(typings)
    await language.warmUp()
  }, 60_000)

  it('types props, events, the context and its hook from the definition', async () => {
    expect(await findErrors(card)).toEqual([])
  })

  it('reports a misused prop', async () => {
    expect(await findErrors(card.replace('count + 1', 'count.length'))).toEqual(
      [expect.stringContaining("'length' does not exist on type 'number'")]
    )
  })

  it('reports an event fired with the wrong object', async () => {
    const source = card.replace('onPick({ kind, count })', 'onPick({ kind })')

    expect(await findErrors(source)).toEqual([
      expect.stringContaining('is not assignable to parameter of type'),
    ])
  })

  it('keeps the options of an enum inside a list item', async () => {
    const source = card.replace(
      "item.tone === 'danger'",
      "item.tone === 'warn'"
    )

    expect(await findErrors(source)).toEqual([
      expect.stringContaining('no overlap'),
    ])
  })

  it('accepts a container that places its children', async () => {
    expect(await findErrors(panel)).toEqual([])
  })

  it('types the props a container declares for its children', async () => {
    const source = panel.replace(
      "child.position.slot === 'header'",
      "child.position.slot === 'footer'"
    )

    expect(await findErrors(source)).toEqual([
      expect.stringContaining('no overlap'),
    ])
  })

  it('tells a container which child is selected in the Designer', async () => {
    const source = panel.replace(
      '<Count />',
      '{ctx.children.find((child) => child.selected)?.position.slot}'
    )

    expect(await findErrors(source)).toEqual([])
    expect(
      await findErrors(
        source.replace(
          'ctx.children.find',
          'ctx.selectedChild; ctx.children.find'
        )
      )
    ).toEqual([expect.stringContaining("'selectedChild' does not exist")])
  })

  it('accepts a container that places its children in child slots', async () => {
    expect(await findErrors(slots)).toEqual([])
  })

  it('reports a child slot with a value the children cannot have', async () => {
    const source = slots.replace(
      "where={{ slot: 'header' }}",
      "where={{ slot: 'footer' }}"
    )

    expect(await findErrors(source)).toHaveLength(1)
  })

  it('makes an optional prop possibly undefined', async () => {
    const source = card.replace(
      'title: prop.string().default(kind)',
      'title: prop.string().optional()'
    )

    expect(await findErrors(source)).toEqual([
      expect.stringContaining("'title' is possibly 'undefined'"),
    ])
  })

  it('makes a prop without a default possibly undefined', async () => {
    const source = card.replace(
      'title: prop.string().default(kind)',
      'title: prop.string()'
    )

    expect(await findErrors(source)).toEqual([
      expect.stringContaining("'title' is possibly 'undefined'"),
    ])
  })

  it('types unions, nullable props and dates', async () => {
    const source = card
      .replace(
        'rows: prop.custom',
        "width: prop.union([prop.literal('auto'), prop.number()]).nullable().default(null),\n      since: prop.date().optional(),\n      rows: prop.custom"
      )
      .replace(
        '<i>{danger.length + rows.length}</i>',
        "<i>{ctx.useProp('since')[0]?.getFullYear()}</i>"
      )
      .replace(
        'const danger =',
        "const width: number | 'auto' | null = ctx.useProp('width')[0]\n      const danger ="
      )

    expect(await findErrors(source)).toEqual([])
  })

  it('reports a variant with a value the prop cannot have', async () => {
    const valid = card.replace(
      'events: {',
      "variants: [{ name: 'Filled', props: { variant: 'filled' } }],\n    events: {"
    )

    expect(await findErrors(valid)).toEqual([])
    expect(
      await findErrors(
        valid.replace("variant: 'filled' }", "variant: 'solid' }")
      )
    ).toHaveLength(1)
  })

  it('reports an event with the name of a prop', async () => {
    const source = card.replace('onReset: event()', 'title: event()')

    expect((await findErrors(source)).length).toBeGreaterThan(0)
  })

  it('types where a child slot places and resizes its children', async () => {
    const source = slots.replace(
      'as="ul"',
      "as=\"ul\" resize={['e', 's']} positionChild={({ bounds, slot, reason }) => ({ slot: reason === 'move' && bounds.x > slot.width / 2 && title ? 'header' : 'content' })}"
    )

    expect(await findErrors(source)).toEqual([])
    expect(
      await findErrors(
        source.replace("? 'header' : 'content'", "? 'header' : 'side'")
      )
    ).toHaveLength(1)
    expect(
      await findErrors(
        source.replace("resize={['e', 's']}", "resize={['east']}")
      )
    ).toHaveLength(1)
  })

  it('types the hook from the prop it names', async () => {
    const source = card.replace('setCount(count + 1)', "setCount('one')")

    expect(await findErrors(source)).toEqual([
      expect.stringContaining(
        "'string' is not assignable to parameter of type 'number'"
      ),
    ])
  })
})
