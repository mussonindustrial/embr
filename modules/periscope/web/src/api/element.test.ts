import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import { createPeriscopeApi } from './periscopeApi'
import { mergeProps } from './root'

const api = createPeriscopeApi({ resource: async () => ({}) as never })
const { defineComponent, prop, event } = api

const Button = defineComponent({
  id: 'button',
  props: {
    text: prop.string().default('Button'),
    variant: prop.enum(['default', 'outline']).default('default'),
  },
  events: { onClick: event() },
  render: ({ text, variant, onClick }, { Root }) =>
    createElement(
      Root,
      { as: 'button', className: `button ${variant}`, onClick },
      text
    ),
})

const Card = defineComponent({
  id: 'card',
  props: { title: prop.string().default('Card') },
  position: { slot: prop.enum(['content', 'footer']).default('content') },
  render: ({ title }, { Root, ChildSlot }) =>
    createElement(
      Root,
      { as: 'section', className: 'card' },
      createElement('h3', null, title),
      createElement(ChildSlot, {
        className: 'content',
        where: { slot: 'content' },
      }),
      createElement(ChildSlot, {
        className: 'footer',
        where: { slot: 'footer' },
      })
    ),
})

const Stack = defineComponent({
  id: 'stack',
  position: { grow: prop.boolean().default(false) },
  render: (_props, { Root, ChildSlot }) =>
    createElement(
      Root,
      { asChild: true, className: 'stack', style: { gap: 4 } },
      createElement(ChildSlot, { as: 'ul', className: 'items' })
    ),
})

const Plain = defineComponent({
  id: 'plain',
  props: { value: prop.number().default(1) },
  render: ({ value }, ctx) => {
    const [count] = ctx.useProp('value')
    return createElement(
      ctx.Root,
      { as: 'span' },
      `${value}:${count}:${ctx.mode}`
    )
  },
})

const Hidden = defineComponent({
  id: 'hidden',
  render: () => null,
})

describe('a library component as a React element', () => {
  it('renders with its defaults and the props it is given', () => {
    expect(renderToStaticMarkup(createElement(Button))).toBe(
      '<button class="button default">Button</button>'
    )
    expect(
      renderToStaticMarkup(
        createElement(Button, { text: 'Save', variant: 'outline' })
      )
    ).toBe('<button class="button outline">Save</button>')
  })

  it('adds a class and style to the outermost element', () => {
    expect(
      renderToStaticMarkup(
        createElement(Button, { className: 'wide', style: { width: 10 } })
      )
    ).toBe(
      '<button class="button default wide" style="width:10px">Button</button>'
    )
  })

  it('reads its props and its mode', () => {
    expect(renderToStaticMarkup(createElement(Plain, { value: 2 }))).toBe(
      '<span>2:2:session</span>'
    )
  })

  it('merges the root onto its only child with asChild', () => {
    expect(
      renderToStaticMarkup(createElement(Stack, { className: 'wide' }))
    ).toBe('<ul class="stack items wide" style="gap:4px"></ul>')
  })

  it('may render nothing without a root', () => {
    expect(renderToStaticMarkup(createElement(Hidden))).toBe('')
  })

  it('runs its own handler and then the one it is given', () => {
    const calls: string[] = []
    const merged = mergeProps(
      { onClick: () => calls.push('perspective'), id: 'a' },
      { onClick: () => calls.push('own'), id: 'b' }
    )

    ;(merged.onClick as () => void)()

    expect(calls).toEqual(['own', 'perspective'])
    expect(merged.id).toBe('b')
  })

  it('shows its children in the slots of a container', () => {
    const markup = renderToStaticMarkup(
      createElement(
        Card,
        {
          title: 'Pump',
          placed: [
            {
              position: { slot: 'footer' },
              node: createElement(Button, { text: 'Stop' }),
            },
          ],
        },
        createElement('p', null, 'Running')
      )
    )

    expect(markup).toBe(
      '<section class="card"><h3>Pump</h3>' +
        '<div class="content"><p>Running</p></div>' +
        '<div class="footer"><button class="button default">Stop</button></div>' +
        '</section>'
    )
  })

  it('is still a component a library can list', () => {
    expect(Button.periscope.id).toBe('button')
    expect(typeof Button.render).toBe('function')
    expect(Button.displayName).toBe('button')
  })
})
