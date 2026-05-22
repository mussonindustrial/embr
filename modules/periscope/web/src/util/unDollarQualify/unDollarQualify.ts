import { get, isArray, isNil, isPlainObject, map, mapValues } from 'lodash'
import { PlainObject } from '@inductiveautomation/perspective-client'

/**
 * Turn a Perspective dollar-qualified object into a plain object.
 * @returns a plain object
 * @param value
 */
export default function unDollarQualify(value: unknown): PlainObject | null {
  if (isNil(value)) {
    return null
  }

  if (isArray(value)) {
    return map(value, unDollarQualify)
  }

  if (isPlainObject(value)) {
    const unwrapped = get(value, '$v')
    if (unwrapped !== undefined) {
      return unDollarQualify(unwrapped)
    }

    return mapValues(value, unDollarQualify)
  }

  return value
}
