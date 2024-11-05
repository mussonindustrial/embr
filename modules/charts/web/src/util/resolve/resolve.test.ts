import { it, expect, describe } from 'vitest'

import resolve from './resolve'

describe('resolve', () => {
    it('return the first input if defined', async () => {
        const result = resolve([1, 2, 3])
        expect(result).toBe(1)
    })
    it('return the first defined input', async () => {
        const result = resolve([undefined, 2, 3])
        expect(result).toBe(2)
    })
    it('treat null as defined', async () => {
        const result = resolve([null, 2, 3])
        expect(result).toBe(null)
    })
    it('return undefined if no inputs are defined', async () => {
        const result = resolve([undefined, undefined, undefined])
        expect(result).toBe(undefined)
    })
})
