import { JsObject } from '@inductiveautomation/perspective-client'
import { merge } from 'lodash'

/**
 * Add globals to the Embr global scope.
 */
export default function addGlobals(globals: JsObject) {
  window.__embrGlobals = merge(window.__embrGlobals, globals)
}
