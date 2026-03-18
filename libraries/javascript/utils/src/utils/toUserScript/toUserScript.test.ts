import { it, expect, describe } from 'vitest'

import toUserScript from './toUserScript'

describe('toUserScript', () => {
  it('run a function without parameters', async () => {
    const result = toUserScript(`() => { return 'testResult' }`).run()
    expect(result).toBe('testResult')
  })

  it('run a function with a single parameter', async () => {
    const result = toUserScript(`(param1) => { return param1; }`).runNamed({
      param1: 'testResult',
    })
    expect(result).toBe('testResult')
  })

  it('run a function without parameters but called with parameters', async () => {
    const result = toUserScript(`() => { return 'testResult' }`).runNamed({
      param1: 'value1',
      param2: 'value2',
    })
    expect(result).toBe('testResult')
  })

  it('run a function without parameters and global parameters', async () => {
    const result = toUserScript(
      `() => { return self }`,
      {},
      {
        self: 'selfValue',
      }
    ).runNamed()
    expect(result).toBe('selfValue')
  })

  it('run a function without parameters and unused global parameters', async () => {
    const result = toUserScript(
      `() => { return 'test' }`,
      {},
      {
        self: 'selfValue',
      }
    ).runNamed()
    expect(result).toBe('test')
  })

  it('run a function with multiple parameters', async () => {
    const script = toUserScript(
      `(param1, param2, param3) => { return param1 + ' ' + param2 + ' ' + param3; }`
    )
    const result = script.runNamed({
      param1: 'result1',
      param2: 'result2',
      param3: 'result3',
    })
    expect(result).toBe('result1 result2 result3')
  })

  it('ignore parameter order', async () => {
    const script = toUserScript(
      `(param1, param2, param3) => { return param1 + ' ' + param2 + ' ' + param3; }`
    )
    const result = script.runNamed({
      param3: 'result3',
      param2: 'result2',
      param1: 'result1',
    })
    expect(result).toBe('result1 result2 result3')
  })

  it('directly callable', async () => {
    const script = toUserScript(
      `(param1, param2, param3) => { return param1 + ' ' + param2 + ' ' + param3; }`
    )
    const result = script('result1', 'result2', 'result3')
    expect(result).toBe('result1 result2 result3')
  })

  it('runnable', async () => {
    const script = toUserScript(
      `(param1, param2, param3) => { return param1 + ' ' + param2 + ' ' + param3; }`
    )
    const result = script.run('result1', 'result2', 'result3')
    expect(result).toBe('result1 result2 result3')
  })

  it('run a function with complex parameters', async () => {
    const script = toUserScript(`(p) => { return p.nested1 + p.nested2; }`)
    const result = script.runNamed({
      p: {
        nested1: 40,
        nested2: 35,
      },
    })
    expect(result).toBe(75)
  })

  it('parse a function with beginning spaces', async () => {
    const script = toUserScript(
      `        (p) => { return p.nested1 + p.nested2; }`
    )
    const result = script.runNamed({
      p: {
        nested1: 67,
        nested2: 32,
      },
    })
    expect(result).toBe(99)
  })

  it('parse a function with ending spaces', async () => {
    const script = toUserScript(`(p) => { return p.nested1 + p.nested2;      }`)
    const result = script.runNamed({
      p: {
        nested1: 140,
        nested2: 135,
      },
    })
    expect(result).toBe(275)
  })

  it('parse a function with beginning and ending spaces', async () => {
    const script = toUserScript(
      `         (p) => { return p.nested1 + p.nested2;  }    `
    )
    const result = script.runNamed({
      p: {
        nested1: 400,
        nested2: 350,
      },
    })
    expect(result).toBe(750)
  })

  it('parse a function with brackets', async () => {
    const script = toUserScript(
      `  (p) => { return p.nested1 + p.nested2; }      `
    )
    const result = script.runNamed({
      p: {
        nested1: 142,
        nested2: 136,
      },
    })
    expect(result).toBe(278)
  })

  it('parse a function with a new line', async () => {
    const script = toUserScript(
      `         (p) => {
                const result = p.nested1 + p.nested2;
                return result;           
      }`
    )
    const result = script.runNamed({
      p: {
        nested1: 400,
        nested2: 350,
      },
    })
    expect(result).toBe(750)
  })

  it('run a function called with custom `this` and global parameters', async () => {
    const script = toUserScript(
      `         ( self, test1 ) => self.nested1 + ' ' + test1 + ' ' + extra.parameter + ' ' + this.prop;      `,
      {
        prop: 'thisProp',
      },
      {
        extra: {
          parameter: 'parameter',
        },
      }
    )
    const result = script.runNamed({
      self: {
        nested1: 400,
        nested2: 350,
      },
      test1: 'a test value',
    })
    expect(result).toBe(`400 a test value parameter thisProp`)
  })

  it('parse a function without a return statement', async () => {
    const result = toUserScript(`() => 'test'`, {
      self: 'selfValue',
    }).runNamed()
    expect(result).toBe('test')
  })

  it('parse an async function', async () => {
    const script = toUserScript(
      ` async (p) => { return p.nested1 + p.nested2; }      `
    )
    const result = script.runNamed({
      p: {
        nested1: 142,
        nested2: 136,
      },
    })
    expect(result).toHaveProperty('then')
    expect(await result).toBe(278)
  })

  it('have correct signature', async () => {
    const script = toUserScript(
      `  (p, param2, testing) => { return p.nested1 + p.nested2; }      `
    )
    expect(script.signature).toStrictEqual(['p', 'param2', 'testing'])
  })

  it('fail to parse a function with an invalid modifier', async () => {
    expect(() =>
      toUserScript(
        ` thisShouldNotBeHere (p) => { return p.nested1 + p.nested2; }      `
      )
    ).toThrowError(ReferenceError('invalid function definition'))
  })

  it('fail to parse a function with an invalid parameter', async () => {
    const script = toUserScript(`() => { return invalidParameter }`)
    expect(() => script.run()).toThrowError(
      ReferenceError('invalidParameter is not defined')
    )
  })

  it('fail strict validation', async () => {
    const script = toUserScript(`() => { 
      x = 3.14;
      return x;
    }`)
    expect(() => script.run()).toThrowError(ReferenceError('x is not defined'))
  })
})
