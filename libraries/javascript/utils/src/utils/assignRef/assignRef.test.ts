import { describe, expect, it, vi } from 'vitest'

import assignRef from './assignRef'

describe('assignRef', () => {
  it('calls a function ref', () => {
    const ref = vi.fn()
    assignRef(ref, 'value')
    expect(ref).toHaveBeenCalledWith('value')
  })

  it('sets an object ref', () => {
    const ref = { current: null as string | null }
    assignRef(ref, 'value')
    expect(ref.current).toBe('value')
  })

  it('ignores a missing ref', () => {
    expect(() => assignRef(undefined, 'value')).not.toThrow()
  })
})
