import { describe, expect, it } from 'vitest'
import { invoke } from '../src'

describe('invoke()', () => {
  it('calls a service method with a JSON payload and answers in JSON', async () => {
    const answer = await invoke(
      'typescript.compiler',
      'compile',
      JSON.stringify({ source: 'const x: number = 1', path: 'test.ts' })
    )

    expect(JSON.parse(answer)).toMatchObject({
      success: true,
      output: expect.stringContaining('const x = 1;'),
    })
  })

  it('answers null for a method that returns nothing', async () => {
    const payload = JSON.stringify({ path: 'missing' })

    expect(await invoke('typescript.language', 'removeFile', payload)).toBe(
      'null'
    )
  })

  it('rejects a service or method it does not have', async () => {
    await expect(invoke('python', 'compile', '{}')).rejects.toThrow(
      'Unknown service: python'
    )
    await expect(
      invoke('typescript.language', 'missing', '{}')
    ).rejects.toThrow('Unknown service method: typescript.language.missing')
  })
})
