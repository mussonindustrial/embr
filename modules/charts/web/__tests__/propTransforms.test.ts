import { it, expect, describe } from 'vitest'
import { getScriptTransform } from '../src'

describe('propTransforms', () => {
  it('transform a script string to a function', async () => {
    const scriptTransform = getScriptTransform()
    const script = scriptTransform(
      `(context, options) => { return context + ' ' + options; }`
    )
    expect(script).toBeTypeOf('function')
  })

  it('should evaluate a script string', async () => {
    const scriptTransform = getScriptTransform()
    const script = scriptTransform(
      `(context, options) => context + ' ' + options;`
    ) as CallableFunction
    expect(script('contextValue', 'optionsValue')).toBe(
      'contextValue optionsValue'
    )
  })

  it('should evaluate a script string with extra context', async () => {
    const scriptTransform = getScriptTransform({ prop: 'propValue' })
    const script = scriptTransform(
      `(context, options) => context + ' ' + options + ' ' + this.prop;`
    ) as CallableFunction
    expect(script('contextValue', 'optionsValue')).toBe(
      'contextValue optionsValue propValue'
    )
  })
})
