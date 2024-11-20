import {
  isFunction,
  toFunction,
  readCSSVar,
  isCSSVar,
  PropTransform,
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
  extraContext: object = {}
): PropTransform<unknown, string | CallableFunction> {
  const transform = (prop: unknown) => {
    if (typeof prop === 'string' && isFunction(prop)) {
      const f = toFunction(prop, extraContext)
      return (...args: unknown[]) => f({ ...args })
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return prop as any
  }
  return transform
}
