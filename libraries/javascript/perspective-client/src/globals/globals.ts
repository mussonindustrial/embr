export type EmbrGlobals = {
  scripting: ScriptingProperties
}

export type ScriptingProperties = {
  globals: Record<string, any>
}

declare global {
  interface Window {
    __embrGlobals?: EmbrGlobals
  }
}

function createDefaultGlobals(): EmbrGlobals {
  window.__embrGlobals = {
    scripting: {
      globals: {},
    },
  }
  return window.__embrGlobals
}

/**
 * Get the Embr global scope.
 */
export function getEmbrGlobals(): EmbrGlobals {
  return window.__embrGlobals ?? createDefaultGlobals()
}
