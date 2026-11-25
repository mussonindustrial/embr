import { describe, expect, it } from 'vitest'
import { findProxyTarget } from '../../../../src/components/embedding/project/proxy-target'

describe('findProxyTarget', () => {
  const element = { tagName: 'DIV' } as unknown as Element

  it('uses the root element when no target is set', () => {
    expect(findProxyTarget(undefined, element)).toBe(element)
  })

  it('uses an explicit target', () => {
    const target = { play() {} }
    expect(findProxyTarget(target, element)).toBe(target)
  })

  it('reads a ref when the script runs', () => {
    const ref: { current: object | null } = { current: null }
    expect(findProxyTarget(ref, element)).toBe(element)
    const audio = { paused: true }
    ref.current = audio
    expect(findProxyTarget(ref, element)).toBe(audio)
  })

  it('is undefined before mount', () => {
    expect(findProxyTarget(undefined, null)).toBeUndefined()
  })
})
