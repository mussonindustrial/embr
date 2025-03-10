import { StyleObject } from '@inductiveautomation/perspective-client'
import uniqueClasses from '../uniqueClasses'
import formatStyleNames from '../formatStyleNames'
import { CSSProperties } from 'react'

/**
 * Emit Perspective StyleObject styling
 * @param style
 * @returns
 */
export default function emitStyles(style: StyleObject): {
  style: CSSProperties
  className?: string
} {
  return {
    className: formatStyleNames(uniqueClasses(style.classes)),
    style,
  }
}
