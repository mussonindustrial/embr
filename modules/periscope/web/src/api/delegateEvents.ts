import type { ComponentDelegate } from './periscopeApi'
import type { Values } from './types'

type Handler = (event: Values) => void

export type DelegateEvents = {
  on: ComponentDelegate['onEvent']
  emit(name: string, event: Values): void
}

export function createDelegateEvents(): DelegateEvents {
  const handlersByName = new Map<string, Set<Handler>>()

  return {
    on: (name, handler) => {
      const handlers = handlersByName.get(name) ?? new Set<Handler>()
      handlersByName.set(name, handlers)
      handlers.add(handler as Handler)

      return () => {
        handlers.delete(handler as Handler)
      }
    },
    emit: (name, event) => {
      handlersByName.get(name)?.forEach((handler) => handler(event))
    },
  }
}

export const detachedDelegate: ComponentDelegate = {
  fireEvent: (name) => {
    console.warn(
      `Cannot fire delegate event '${name}', the component is not placed in a view.`
    )
  },
  onEvent: () => () => {},
}
