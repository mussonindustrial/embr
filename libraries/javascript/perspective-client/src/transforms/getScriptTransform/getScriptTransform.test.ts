import { it, expect, describe } from 'vitest'
import { default as getScriptTransform } from './index'
import { ComponentStore } from '@inductiveautomation/perspective-client'

describe('getScriptTransform', () => {
  it('transform a script string to a function', async () => {
    const thisArg = { test: 'test' }
    const component = {
      view: {
        page: {
          parent: 'client',
        },
      },
    } as unknown as ComponentStore

    const scriptTransform = getScriptTransform(thisArg, component)
    const script = scriptTransform(
      `(context, options) => { return context + ' ' + options; }`
    )
    expect(script).toBeTypeOf('function')
  })

  it('should evaluate a script string', async () => {
    const thisArg = { test: 'test' }
    const component = {
      view: {
        page: {
          parent: 'client',
        },
      },
    } as unknown as ComponentStore

    const scriptTransform = getScriptTransform(thisArg, component)
    const script = scriptTransform(
      `(context, options) => context + ' ' + options;`
    ) as CallableFunction
    expect(script('contextValue', 'optionsValue')).toBe(
      'contextValue optionsValue'
    )
  })

  it('should evaluate a script string with thisArg and scripting globals', async () => {
    const thisArg = { test: 'test' }
    const component = {
      view: {
        page: {
          parent: 'client',
        },
      },
    } as unknown as ComponentStore

    const scriptTransform = getScriptTransform(thisArg, component)
    const script = scriptTransform(
      `(context, options) => context + ' ' + options + ' ' + this.test + ' ' + perspective.context.client;`
    ) as CallableFunction
    expect(script('contextValue', 'optionsValue')).toBe(
      'contextValue optionsValue test client'
    )
  })
})
