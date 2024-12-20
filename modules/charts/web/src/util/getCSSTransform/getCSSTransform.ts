import { isCSSVar, PropTransform, readCSSVar } from '@embr-js/utils'

export default function getCSSTransform(
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
