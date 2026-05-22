import { ComponentStore } from '@inductiveautomation/perspective-client'
import { isFunction, PropTransform, toUserScript } from '@embr-js/utils'

export default function getScriptTransform(
  thisArg: object = {},
  component: ComponentStore
): PropTransform<unknown, string | CallableFunction> {
  return (prop: unknown) => {
    if (typeof prop === 'string' && isFunction(prop)) {
      const f = toUserScript(prop, thisArg, () =>
        Embr.scripting.createGlobals({
          client: component.view.page.parent,
          page: component.view.page,
          view: component.view,
          component: component,
        })
      )
      return (...args: unknown[]) => f(...args)
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return prop as any
  }
}
