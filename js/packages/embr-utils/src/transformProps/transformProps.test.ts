import { it, expect, describe } from 'vitest'

import { type PropTransform } from './transformProps'
import transformProps from './transformProps'

const addOne: PropTransform<unknown, unknown> = (prop: unknown) => {
    if (typeof prop === 'number') {
        return prop + 1
    } else {
        return prop
    }
}

const multiplyTen: PropTransform<unknown, unknown> = (prop: unknown) => {
    if (typeof prop === 'number') {
        return prop * 10
    } else {
        return prop
    }
}

describe('transformProps', () => {
    it('transform a dictionary of numbers', async () => {
        const test = {
            p1: 1,
            p2: 3,
            p3: 5,
        }

        const result = transformProps(test, [addOne])
        expect(result).toEqual({ p1: 2, p2: 4, p3: 6 })
    })

    it('transform a nested dictionary of numbers', async () => {
        const test = {
            p1: 1,
            p2: 3,
            p3: 5,
            p4: {
                p5: 9,
            },
        }

        const result = transformProps(test, [addOne])
        expect(result).toEqual({
            p1: 2,
            p2: 4,
            p3: 6,
            p4: {
                p5: 10,
            },
        })
    })

    it('transform a nested dictionary of numbers with multiple transformers', async () => {
        const test = {
            p1: 1,
            p2: 3,
            p3: 5,
            p4: {
                p5: 9,
            },
        }

        const result = transformProps(test, [addOne, multiplyTen])
        expect(result).toEqual({
            p1: 20,
            p2: 40,
            p3: 60,
            p4: {
                p5: 100,
            },
        })
    })

    it('handle date types', async () => {
        const now = new Date()
        const test = {
            a: 1,
            b: {
                c: 2,
                d: now,
            },
        }

        const result = transformProps(test, [addOne, multiplyTen])
        expect(result).toEqual({
            a: 20,
            b: {
                c: 30,
                d: now,
            },
        })
    })
})
