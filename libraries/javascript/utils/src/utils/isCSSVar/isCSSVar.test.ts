import { it, expect, describe } from 'vitest'

import isCSSVar from './isCSSVar'

describe('isCSSVar', () => {
  it('false if not a css variable', async () => {
    expect(isCSSVar('no css variable tag here')).toBe(false)
  })

  it('true if a css variable', async () => {
    expect(isCSSVar('var(--a-css-property)')).toBe(true)
  })

  it('true if a css variable (with extra spaces)', async () => {
    expect(isCSSVar('    var(--a-css-property)')).toBe(true)
  })
})
