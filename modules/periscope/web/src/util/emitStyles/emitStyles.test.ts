import { it, expect, describe } from 'vitest'

import emitStyles from './emitStyles'

describe('emitStyles', () => {
  it('emit style information', async () => {
    const result = emitStyles({
      backgroundColor: 'red',
      color: 'blue',
      classes: 'TestClass',
    })
    expect(result).toEqual({
      style: {
        backgroundColor: 'red',
        color: 'blue',
        classes: 'TestClass',
      },
      className: 'psc-TestClass',
    })
  })
  it('handle list of classes', async () => {
    const result = emitStyles({
      backgroundColor: 'red',
      color: 'blue',
      classes: ['TestClass', 'TestClass2', 'TestClass3'],
    })
    expect(result).toEqual({
      style: {
        backgroundColor: 'red',
        color: 'blue',
        classes: ['TestClass', 'TestClass2', 'TestClass3'],
      },
      className: 'psc-TestClass psc-TestClass2 psc-TestClass3',
    })
  })
})
