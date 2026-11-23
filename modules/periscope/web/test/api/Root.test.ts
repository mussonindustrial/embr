import { describe, expect, it } from 'vitest'
import { mergeProps } from '../../src/api/Root'

describe('mergeProps', () => {
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
})
