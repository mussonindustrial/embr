import { describe, expect, it } from 'vitest'
import { getIn, replaceEqualDeep, setIn } from '../../src/api/useProp'

describe('getIn', () => {
  it('reads nested props', () => {
    const props = {
      options: [{ label: 'A' }, { label: 'B' }],
      config: { on: true },
    }

    expect(getIn(props, 'options[1].label')).toBe('B')
    expect(getIn(props, 'config.on')).toBe(true)
    expect(getIn(props, 'missing.deep')).toBeUndefined()
  })
})

describe('setIn', () => {
  it('sets a nested value on a copy, creating what is missing', () => {
    const source = { a: { b: 1 }, list: [{ x: 1 }] }

    expect(setIn(source, ['a', 'c', 'd'], 2)).toEqual({
      a: { b: 1, c: { d: 2 } },
      list: [{ x: 1 }],
    })
    expect(setIn(source, ['list', '0', 'x'], 5)).toEqual({
      a: { b: 1 },
      list: [{ x: 5 }],
    })
    expect(setIn(undefined, ['name'], 'ben')).toEqual({ name: 'ben' })
    expect(setIn(undefined, ['options', '0', 'label'], 'a')).toEqual({
      options: [{ label: 'a' }],
    })
    expect(setIn({ name: 'text' }, ['name', 'first'], 'b')).toEqual({
      name: { first: 'b' },
    })
    expect(source).toEqual({ a: { b: 1 }, list: [{ x: 1 }] })
  })
})

describe('replaceEqualDeep', () => {
  it('keeps the identity of everything that did not change', () => {
    const previous = {
      title: 'Pump',
      rows: [{ id: 1 }, { id: 2 }],
      config: { on: true },
    }
    const next = {
      title: 'Pump',
      rows: [{ id: 1 }, { id: 3 }],
      config: { on: true },
    }

    const shared = replaceEqualDeep(previous, next)

    expect(shared).toEqual(next)
    expect(shared).not.toBe(previous)
    expect(shared.config).toBe(previous.config)
    expect(shared.rows).not.toBe(previous.rows)
    expect(shared.rows[0]).toBe(previous.rows[0])
    expect(shared.rows[1]).not.toBe(previous.rows[1])
  })

  it('returns the previous value when nothing changed', () => {
    const previous = { rows: [{ id: 1 }], style: { classes: '' } }

    expect(
      replaceEqualDeep(previous, { rows: [{ id: 1 }], style: { classes: '' } })
    ).toBe(previous)
    expect(replaceEqualDeep(previous, { rows: [{ id: 1 }] })).not.toBe(previous)
    expect(replaceEqualDeep([1, 2], { 0: 1, 1: 2 })).toEqual({ 0: 1, 1: 2 })
  })
})
