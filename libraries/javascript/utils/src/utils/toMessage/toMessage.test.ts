import { describe, expect, it } from 'vitest'

import toMessage from './toMessage'

describe('toMessage', () => {
  it('reads the message of an error', () => {
    expect(toMessage(new Error('failed'))).toBe('failed')
  })

  it('converts anything else to a string', () => {
    expect(toMessage('failed')).toBe('failed')
    expect(toMessage(undefined)).toBe('undefined')
  })
})
