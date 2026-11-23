import { describe, expect, expectTypeOf, it } from 'vitest'
import {
  createPeriscopeApi,
  event,
  InferProps,
  prop,
} from '../../src/api/periscopeApi'

const { defineComponent } = createPeriscopeApi({
  resource: async () => ({}),
  source: () => 'ui/test',
})

describe('prop', () => {
  it('builds the schema of a prop by chaining', () => {
    expect(
      prop.number().int().min(0).max(10).step(2).default(4).describe('Rows')
        .schema
    ).toEqual({
      type: 'integer',
      minimum: 0,
      maximum: 10,
      multipleOf: 2,
      default: 4,
      description: 'Rows',
    })
  })

  it('keeps the number methods after optional and nullable', () => {
    const rows = prop.number().optional().nullable().int().min(0)
    const count: number | null | undefined = rows.type

    expect(count).toBeUndefined()
    expect(rows.isOptional).toBe(true)
    expect(rows.schema).toEqual({ type: ['integer', 'null'], minimum: 0 })
  })

  it('does not change the prop a chain starts from', () => {
    const base = prop.string()
    base.default('a').describe('b').optional()

    expect(base.schema).toEqual({ type: 'string' })
    expect(base.isOptional).toBe(false)
  })

  it('requires the props that are not optional and have no default', () => {
    const { schema } = prop.object({
      id: prop.string(),
      label: prop.string().default(''),
      note: prop.string().optional(),
    })

    expect(schema.required).toEqual(['id'])
    expect(schema.default).toEqual({ label: '' })
  })

  it('gives a component the props without a default as optional', () => {
    const props = {
      label: prop.string(),
      size: prop.number().default(1),
      rows: prop.number().nullable().default(null).int(),
      point: prop.object({ x: prop.number(), y: prop.number() }),
    }

    expectTypeOf<InferProps<typeof props>>().toEqualTypeOf<{
      size: number
      rows: number | null
      point: { x: number; y: number }
      label?: string
    }>()
    expect(prop.string().hasDefault).toBe(false)
    expect(props.size.nullable().hasDefault).toBe(true)
  })

  it('lets a prop be null', () => {
    expect(prop.string().nullable().schema).toEqual({
      type: ['string', 'null'],
    })
    expect(prop.enum(['a', 'b']).nullable().schema).toEqual({
      type: ['string', 'null'],
      enum: ['a', 'b', null],
    })
    expect(prop.style().nullable().schema).toMatchObject({
      anyOf: [{ $ref: expect.any(String) }, { type: 'null' }],
      default: { classes: '' },
    })
  })

  it('describes unions, literals, dates and custom schemas', () => {
    expect(
      prop.union([prop.literal('auto'), prop.number().min(0)]).schema
    ).toEqual({
      anyOf: [
        { type: 'string', enum: ['auto'] },
        { type: 'number', minimum: 0 },
      ],
    })
    expect(prop.date().nullable().schema).toEqual({ type: ['date', 'null'] })
    expect(
      prop.custom<string>({ type: 'string', pattern: '^[a-z]+$' }).default('a')
        .schema
    ).toEqual({ type: 'string', pattern: '^[a-z]+$', default: 'a' })
  })
})

describe('event', () => {
  it('describes an event and its payload', () => {
    const picked = event({ id: prop.string() }).describe('Picked')

    expect(picked.description).toBe('Picked')
    expect(picked.schema).toEqual({
      type: 'object',
      properties: { id: { type: 'string' } },
      required: ['id'],
    })
    expect(event().schema).toEqual({ type: 'object', properties: {} })
  })
})

describe('defineComponent', () => {
  it('keeps the position props and variants of a component', () => {
    const component = defineComponent({
      props: { gap: prop.number().default(8) },
      position: { grow: prop.boolean().default(false) },
      variants: [{ name: 'Wide', props: { gap: 24 } }],
      render: () => null,
    })

    expect(component.periscope).toMatchObject({
      defaults: { gap: 8 },
      position: { defaults: { grow: false } },
      variants: [{ name: 'Wide', props: { gap: 24 } }],
    })
  })

  it('requires no prop or position prop, since a view may leave any out', () => {
    const component = defineComponent({
      props: {
        title: prop.string(),
        point: prop.object({ x: prop.number() }),
      },
      position: { slot: prop.string() },
      render: () => null,
    })

    const { schema, position } = component.periscope
    expect(schema).not.toHaveProperty('required')
    expect(position?.schema).not.toHaveProperty('required')
    expect(schema.properties.point).toMatchObject({ required: ['x'] })
  })

  it('notes the resource that defined a component', () => {
    const component = defineComponent({ render: () => null })

    expect(component.periscope.source).toBe('ui/test')
  })

  it('reports a name used by both a prop and an event', () => {
    const component = defineComponent({
      props: { change: prop.string().default('') },
      events: { change: event() } as never,
      render: () => null,
    })

    expect(component.periscope.diagnostics).toEqual([
      "'change' is both a prop and an event.",
    ])
  })
})
