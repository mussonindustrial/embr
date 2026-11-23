import { describe, expect, it } from 'vitest'

import messageOf from './messageOf'

describe('messageOf', () => {
  it('reads the message of an error', () => {
    expect(messageOf(new Error('failed'))).toBe('failed')
  })

  it('converts anything else to a string', () => {
    expect(messageOf('failed')).toBe('failed')
    expect(messageOf(undefined)).toBe('undefined')
  })
})
