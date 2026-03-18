import { CSSProperties } from 'react'
import { uniqueClasses } from '@embr-js/utils'
import { StyleObject } from '@inductiveautomation/perspective-client'

import formatStyleNames from '../formatStyleNames'

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
