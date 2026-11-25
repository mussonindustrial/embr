import React from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import { createPeriscopeApi } from '../../src/api/periscopeApi'

const api = createPeriscopeApi({ resource: async () => ({}) as never })
const { defineComponent, prop, event } = api

const Button = defineComponent({
  props: {
    text: prop.string().default('Button'),
    variant: prop.enum(['default', 'outline']).default('default'),
  },
  events: { onClick: event() },
  render: ({ text, variant, onClick }, { Root }) => (
    <Root as="button" className={`button ${variant}`} onClick={onClick}>
      {text}
    </Root>
  ),
})

const Card = defineComponent({
  props: { title: prop.string().default('Card') },
  position: { slot: prop.enum(['content', 'footer']).default('content') },
  render: ({ title }, { Root, ChildSlot }) => (
    <Root as="section" className="card">
      <h3>{title}</h3>
      <ChildSlot className="content" where={{ slot: 'content' }} />
      <ChildSlot className="footer" where={{ slot: 'footer' }} />
    </Root>
  ),
})

const Stack = defineComponent({
  position: { grow: prop.boolean().default(false) },
  render: (_props, { Root, ChildSlot }) => (
    <Root asChild className="stack" style={{ gap: 4 }}>
      <ChildSlot as="ul" className="items" />
    </Root>
  ),
})

const Plain = defineComponent({
  props: { value: prop.number().default(1) },
  render: ({ value }, ctx) => {
    const [count] = ctx.useProp('value')
    return <ctx.Root as="span">{`${value}:${count}:${ctx.mode}`}</ctx.Root>
  },
})

const Hidden = defineComponent({
  render: () => null,
})

describe('useElement', () => {
  it('renders with its defaults and the props it is given', () => {
    expect(renderToStaticMarkup(<Button />)).toBe(
      '<button class="button default">Button</button>'
    )
    expect(renderToStaticMarkup(<Button text="Save" variant="outline" />)).toBe(
      '<button class="button outline">Save</button>'
    )
  })

  it('adds a class and style to the outermost element', () => {
    expect(
      renderToStaticMarkup(<Button className="wide" style={{ width: 10 }} />)
    ).toBe(
      '<button class="button default wide" style="width:10px">Button</button>'
    )
  })

  it('reads its props and its mode', () => {
    expect(renderToStaticMarkup(<Plain value={2} />)).toBe(
      '<span>2:2:session</span>'
    )
  })

  it('merges the root onto its only child with asChild', () => {
    expect(renderToStaticMarkup(<Stack className="wide" />)).toBe(
      '<ul class="stack items wide" style="gap:4px"></ul>'
    )
  })

  it('may render nothing without a root', () => {
    expect(renderToStaticMarkup(<Hidden />)).toBe('')
  })

  it('shows its children in the slots of a container', () => {
    const markup = renderToStaticMarkup(
      <Card
        title="Pump"
        placed={[
          { position: { slot: 'footer' }, node: <Button text="Stop" /> },
        ]}
      >
        <p>Running</p>
      </Card>
    )

    expect(markup).toBe(
      '<section class="card"><h3>Pump</h3>' +
        '<div class="content"><p>Running</p></div>' +
        '<div class="footer"><button class="button default">Stop</button></div>' +
        '</section>'
    )
  })
})
