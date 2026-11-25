import { describe, expect, it } from 'vitest'
import { readComponent } from '../../src/api/component'
import { createPeriscopeApi } from '../../src/api/periscopeApi'

const { defineComponent } = createPeriscopeApi({ resource: async () => ({}) })

describe('readComponent', () => {
  it('reads the component a resource exports', () => {
    const card = defineComponent({ render: () => null })

    expect(readComponent({ default: card })).toBe(card)
  })

  it('requires the default export to be made with defineComponent', () => {
    expect(() => readComponent({ default: () => null })).toThrow(
      'defineComponent'
    )
    expect(() => readComponent({ default: { render: () => null } })).toThrow(
      'defineComponent'
    )
    expect(() => readComponent({})).toThrow('defineComponent')
    expect(() => readComponent(null)).toThrow('defineComponent')
  })
})
