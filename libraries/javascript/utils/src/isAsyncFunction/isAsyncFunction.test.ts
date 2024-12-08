import { it, expect, describe } from 'vitest'

import isFunction from '../../../../../libraries/javascript/utils/src/isAsyncFunction/isAsyncFunction'
import isAsyncFunction from '../../../../../libraries/javascript/utils/src/isAsyncFunction/isAsyncFunction'

describe('isAsyncFunction', () => {
  it('false if no arrow function', async () => {
    expect(isAsyncFunction('no arrow function here')).toBe(false)
  })

  it('true if async arrow function', async () => {
    expect(isAsyncFunction('async () => arrow function here')).toBe(true)
  })

  it('true if arrow function with extra spaces', async () => {
    expect(isFunction('  async  () => arrow function here          ')).toBe(
      true
    )
  })

  it('true if arrow function with new lines', async () => {
    expect(
      isFunction(`  async  () => arrow 
        function here          `)
    ).toBe(true)
  })

  it('true if arrow function parameters', async () => {
    expect(
      isFunction(
        '    async(parameter1, parameter2) => arrow function here          '
      )
    ).toBe(true)
  })
})
