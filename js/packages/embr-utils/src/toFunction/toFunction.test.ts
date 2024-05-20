import { it, expect, describe } from 'vitest'

import toFunction from './toFunction'

describe('toFunction', () => {
    it('parse a function without parameters', async () => {
        const result = toFunction(`return 'testResult'`)()
        expect(result).toBe('testResult')
    })

    it('parse a function with a single parameter', async () => {
        const result = toFunction(`return param1;`)({ param1: 'testResult' })
        expect(result).toBe('testResult')
    })

    it('parse a function with multiple parameters', async () => {
        const script = toFunction(
            `return param1 + ' ' + param2 + ' ' + param3;`
        )
        const result = script({
            param1: 'result1',
            param2: 'result2',
            param3: 'result3',
        })
        expect(result).toBe('result1 result2 result3')
    })

    it('parse a function with complex parameters', async () => {
        const script = toFunction(`return p.nested1 + p.nested2;`)
        const result = script({
            p: {
                nested1: 40,
                nested2: 35,
            },
        })
        expect(result).toBe(75)
    })

    it('parse a function with a beginning script tag', async () => {
        const script = toFunction(`<script> return p.nested1 + p.nested2;`)
        const result = script({
            p: {
                nested1: 40,
                nested2: 35,
            },
        })
        expect(result).toBe(75)
    })

    it('parse a function with a beginning script tag with spaces', async () => {
        const script = toFunction(
            `        <script> return p.nested1 + p.nested2;`
        )
        const result = script({
            p: {
                nested1: 67,
                nested2: 32,
            },
        })
        expect(result).toBe(99)
    })

    it('should parse a function with an ending script tag', async () => {
        const script = toFunction(`return p.nested1 + p.nested2;</script>`)
        const result = script({
            p: {
                nested1: 10,
                nested2: 23,
            },
        })
        expect(result).toBe(33)
    })

    it('should parse a function with an ending script tag with spaces', async () => {
        const script = toFunction(
            `return p.nested1 + p.nested2;</script>      `
        )
        const result = script({
            p: {
                nested1: 140,
                nested2: 135,
            },
        })
        expect(result).toBe(275)
    })

    it('should parse a function with beginning and ending script tags with spaces', async () => {
        const script = toFunction(
            `<script>   return p.nested1 + p.nested2;</script>      `
        )
        const result = script({
            p: {
                nested1: 400,
                nested2: 350,
            },
        })
        expect(result).toBe(750)
    })

    it('should fail to parse a function with an invalid parameter', async () => {
        const script = toFunction(`return invalidParameter`)
        expect(script).toThrowError(
            ReferenceError('invalidParameter is not defined')
        )
    })
})
