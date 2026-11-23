import { describe, expect, it } from 'vitest'
import { componentOf } from '../../src/api/component'
import { createPeriscopeApi } from '../../src/api/periscopeApi'

const { defineComponent } = createPeriscopeApi({ resource: async () => ({}) })

describe('componentOf', () => {
  it('reads the component a resource exports', () => {
    const card = defineComponent({ render: () => null })

    expect(componentOf({ default: card })).toBe(card)
  })

  it('requires the default export to be made with defineComponent', () => {
    expect(() => componentOf({ default: () => null })).toThrow(
      'defineComponent'
    )
    expect(() => componentOf({ default: { render: () => null } })).toThrow(
      'defineComponent'
    )
    expect(() => componentOf({})).toThrow('defineComponent')
    expect(() => componentOf(null)).toThrow('defineComponent')
  })
})
