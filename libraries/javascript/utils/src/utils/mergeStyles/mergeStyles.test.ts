import { it, expect, describe } from 'vitest'

import mergeStyles from './mergeStyles'

describe('mergeStyles', () => {
  it('merge classes', async () => {
    const result = mergeStyles([
      {
        classes: 'class1',
      },
      {
        classes: 'class2',
      },
      {
        classes: 'class3',
      },
    ])
    expect(result).toEqual({
      classes: ['class1', 'class2', 'class3'],
    })
  })
  it('deduplicate classes', async () => {
    const result = mergeStyles([
      {
        classes: 'class1 class2',
      },
      {
        classes: ['class2', 'class1', 'class3'],
      },
      {
        classes: 'class3',
      },
    ])
    expect(result).toEqual({
      classes: ['class1', 'class2', 'class3'],
    })
  })
  it('merge styles', async () => {
    const result = mergeStyles([
      {
        background: 'red',
        classes: 'class1',
      },
      {
        color: 'blue',
        classes: 'class2',
      },
      {
        border: 'none',
        classes: 'class3',
      },
    ])
    expect(result).toEqual({
      background: 'red',
      color: 'blue',
      border: 'none',
      classes: ['class1', 'class2', 'class3'],
    })
  })
  it('use the last specified style property', async () => {
    const result = mergeStyles([
      {
        background: 'red',
      },
      {
        background: 'blue',
      },
      {
        background: 'green',
      },
    ])
    expect(result).toEqual({
      background: 'green',
    })
  })
})
