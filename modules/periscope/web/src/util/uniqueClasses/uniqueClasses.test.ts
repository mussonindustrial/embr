import { it, expect, describe } from 'vitest'

import uniqueClasses from './uniqueClasses'

describe('uniqueClasses', () => {
  it('not modify unique classes from list', async () => {
    const result = uniqueClasses(['test', 'test2', 'test3'])
    expect(result).toEqual(['test', 'test2', 'test3'])
  })
  it('remove duplicate classes from list', async () => {
    const result = uniqueClasses([
      'test',
      'test2',
      'test',
      'test3',
      'test',
      'test',
    ])
    expect(result).toEqual(['test', 'test2', 'test3'])
  })
  it('not modify unique classes from string', async () => {
    const result = uniqueClasses('test test2 test3')
    expect(result).toEqual(['test', 'test2', 'test3'])
  })
  it('remove duplicate classes from string', async () => {
    const result = uniqueClasses('test test2 test test3 test test')
    expect(result).toEqual(['test', 'test2', 'test3'])
  })
  it('return same results for string and list', async () => {
    const result1 = uniqueClasses('test test2 test3')
    const result2 = uniqueClasses(['test', 'test2', 'test3'])
    expect(result1).toEqual(result2)
  })
})
