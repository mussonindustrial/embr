import { it, expect, describe } from 'vitest'

import isFunction from './isFunction'

describe('isFunction', () => {
  it('false if no arrow function', async () => {
    expect(isFunction('no arrow function here')).toBe(false)
  })

  it('true if arrow function', async () => {
    expect(isFunction('() => arrow function here')).toBe(true)
  })

  it('true if arrow function with extra spaces', async () => {
    expect(isFunction('    () => arrow function here          ')).toBe(true)
  })

  it('true if arrow function with new lines', async () => {
    expect(
      isFunction(`    () => arrow 
        function here          `)
    ).toBe(true)
  })

  it('true if arrow function parameters', async () => {
    expect(
      isFunction(
        '    (parameter1, parameter2) => arrow function here          '
      )
    ).toBe(true)
  })
})
