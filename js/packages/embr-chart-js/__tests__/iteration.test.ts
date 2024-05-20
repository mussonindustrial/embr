import { it, expect, describe } from 'vitest'
import { recursiveMap } from '../src/util'

describe('iteration', () => {
    it('should map a simple function', async () => {
        const test = {
            a: 1,
            b: 2,
        }
        const fn = (value: unknown) => {
            if (typeof value === 'number') {
                return value * 2
            }
            return value
        }
        expect(recursiveMap(test, fn)).toEqual({ a: 2, b: 4 })
    })

    it('should map a nested object', async () => {
        const test = {
            a: 1,
            b: {
                c: 2,
                d: 4,
            },
        }
        const fn = (value: unknown) => {
            if (typeof value === 'number') {
                return value * 2
            }
            return value
        }
        expect(recursiveMap(test, fn)).toEqual({ a: 2, b: { c: 4, d: 8 } })
    })

    it('should map a nested object with dates', async () => {
        const now = new Date()
        const test = {
            a: 1,
            b: {
                c: 2,
                d: now,
            },
        }
        const fn = (value: unknown) => {
            if (typeof value === 'number') {
                return value * 2
            }
            return value
        }
        expect(recursiveMap(test, fn)).toEqual({ a: 2, b: { c: 4, d: now } })
    })
})
