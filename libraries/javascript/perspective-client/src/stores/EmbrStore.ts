import { ScriptingStore, ScriptingStoreImpl } from './ScriptingStore'

class EmbrStoreBase {
  scripting: ScriptingStore = new ScriptingStoreImpl()
}

// eslint-disable-next-line
export interface EmbrStoreExtensions {}
export type EmbrStore = EmbrStoreBase & EmbrStoreExtensions

declare global {
  interface Window {
    Embr: EmbrStore
  }

  // eslint-disable-next-line no-var
  var Embr: EmbrStore
}

function createEmbrStore(): EmbrStore {
  return new EmbrStoreBase() as EmbrStore
}

if (globalThis.Embr == null) {
  Object.defineProperty(globalThis, 'Embr', {
    value: createEmbrStore(),
    writable: false,
    configurable: false,
  })
}

export const Embr = globalThis.Embr

/**
 * Register a substore once.
 */
export function registerStore<K extends keyof EmbrStoreExtensions>(
  key: K,
  create: () => EmbrStoreExtensions[K]
): EmbrStoreExtensions[K] {
  const store = Embr as EmbrStore & Record<string, unknown>

  if (store[key as string] == null) {
    Object.defineProperty(store, key, {
      value: create(),
      writable: false,
      configurable: false,
    })
  }

  return store[key as string] as EmbrStoreExtensions[K]
}
