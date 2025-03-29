import { JsObject } from '@inductiveautomation/perspective-client'

export type EmbrGlobals = JsObject & {
  scriptingGlobals?: JsObject
}

declare global {
  interface Window {
    __embrGlobals?: EmbrGlobals
  }
}

/**
 * Get the Embr global scope.
 */
export default function getGlobals() {
  return window.__embrGlobals ?? {}
}
