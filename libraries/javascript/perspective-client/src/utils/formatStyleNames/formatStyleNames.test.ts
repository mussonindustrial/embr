import { it, expect, describe } from 'vitest'

import formatStyleNames from './formatStyleNames'

describe('formatStyleNames', () => {
  it('format single style as string', async () => {
    const result = formatStyleNames('testClass')
    expect(result).toEqual('psc-testClass')
  })
  it('format multiple styles as string', async () => {
    const result = formatStyleNames('testClass testClass2 testClass3')
    expect(result).toEqual('psc-testClass psc-testClass2 psc-testClass3')
  })
  it('format single style as list', async () => {
    const result = formatStyleNames(['testClass'])
    expect(result).toEqual('psc-testClass')
  })
  it('format multiple styles as list', async () => {
    const result = formatStyleNames(['testClass', 'testClass2', 'testClass3'])
    expect(result).toEqual('psc-testClass psc-testClass2 psc-testClass3')
  })
  it('return same results for string and list', async () => {
    const result1 = formatStyleNames('testClass testClass2 testClass3')
    const result2 = formatStyleNames(['testClass', 'testClass2', 'testClass3'])
    expect(result1).toEqual(result2)
  })
})
