import { it, expect, describe } from 'vitest'

import toFunction from './toFunction'

describe('toFunction', () => {
    it('parse a function without parameters', async () => {
        const result = toFunction(`() => return 'testResult'`)()
        expect(result).toBe('testResult')
    })

    it('parse a function with a single parameter', async () => {
        const result = toFunction(`(param1) => return param1;`)({
            param1: 'testResult',
        })
        expect(result).toBe('testResult')
    })

    it('parse a function without parameters but called with parameters', async () => {
        const result = toFunction(`() => return 'testResult'`)({
            param1: 'value1',
            param2: 'value2',
        })
        expect(result).toBe('testResult')
    })

    it('parse a function without parameters and global parameters', async () => {
        const result = toFunction(`() => return self`, {
            self: 'selfValue',
        })()
        expect(result).toBe('selfValue')
    })

    it('parse a function without parameters and unused global parameters', async () => {
        const result = toFunction(`() => return 'test'`, {
            self: 'selfValue',
        })()
        expect(result).toBe('test')
    })

    it('parse a function with multiple parameters', async () => {
        const script = toFunction(
            `(param1, param2, param3) => return param1 + ' ' + param2 + ' ' + param3;`
        )
        const result = script({
            param1: 'result1',
            param2: 'result2',
            param3: 'result3',
        })
        expect(result).toBe('result1 result2 result3')
    })

    it('parse a function with complex parameters', async () => {
        const script = toFunction(`(p) => return p.nested1 + p.nested2;`)
        const result = script({
            p: {
                nested1: 40,
                nested2: 35,
            },
        })
        expect(result).toBe(75)
    })

    it('parse a function with a beginning script tag', async () => {
        const script = toFunction(`(p) => return p.nested1 + p.nested2;`)
        const result = script({
            p: {
                nested1: 40,
                nested2: 35,
            },
        })
        expect(result).toBe(75)
    })

    it('parse a function with beginning spaces', async () => {
        const script = toFunction(
            `        (p) => return p.nested1 + p.nested2;`
        )
        const result = script({
            p: {
                nested1: 67,
                nested2: 32,
            },
        })
        expect(result).toBe(99)
    })

    it('should parse a function with ending spaces', async () => {
        const script = toFunction(`(p) => return p.nested1 + p.nested2;      `)
        const result = script({
            p: {
                nested1: 140,
                nested2: 135,
            },
        })
        expect(result).toBe(275)
    })

    it('should parse a function with beginning and ending spaces', async () => {
        const script = toFunction(
            `         (p) => return p.nested1 + p.nested2;      `
        )
        const result = script({
            p: {
                nested1: 400,
                nested2: 350,
            },
        })
        expect(result).toBe(750)
    })

    it('should parse a function with a new line', async () => {
        const script = toFunction(
            `         (p) => 
                const result = p.nested1 + p.nested2;
                return result;      `
        )
        const result = script({
            p: {
                nested1: 400,
                nested2: 350,
            },
        })
        expect(result).toBe(750)
    })

    it('should run a function called with global parameters', async () => {
        const script = toFunction(
            `         ( self, test1 ) => return self.nested1 + ' ' + test1 + ' ' + extra.parameter;      `,
            {
                extra: {
                    parameter: 'parameter',
                },
            }
        )
        const result = script({
            self: {
                nested1: 400,
                nested2: 350,
            },
            test1: 'a test value',
        })
        expect(result).toBe(`400 a test value parameter`)
    })

    it('should fail to parse a function with an invalid parameter', async () => {
        const script = toFunction(`() => return invalidParameter`)
        expect(script).toThrowError(
            ReferenceError('invalidParameter is not defined')
        )
    })
})
