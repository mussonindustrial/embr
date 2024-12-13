import {
  isCSSVar,
  isFunction,
  PropTransform,
  readCSSVar,
  toUserScript,
} from '@embr-js/utils'

export function getCSSTransform(
  element: Element | null | undefined
): PropTransform<unknown, unknown> {
  return (prop: unknown) => {
    if (
      element !== null &&
      element !== undefined &&
      typeof prop === 'string' &&
      isCSSVar(prop)
    ) {
      return readCSSVar(element, prop)
    }
    return prop
  }
}

export function getScriptTransform(
  thisArg: object = {}
): PropTransform<unknown, string | CallableFunction> {
  return (prop: unknown) => {
    if (typeof prop === 'string' && isFunction(prop)) {
      const f = toUserScript(prop, thisArg)
      return (...args: unknown[]) => f(...args)
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return prop as any
  }
}
