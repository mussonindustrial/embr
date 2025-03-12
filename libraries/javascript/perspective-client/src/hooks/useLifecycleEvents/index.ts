import { useEffect } from 'react'

export type ComponentLifecycleEvent = (context?: unknown) => void

export type ComponentLifecycleEvents = {
  onMount?: ComponentLifecycleEvent
  onUpdate?: ComponentLifecycleEvent
  onUnmount?: ComponentLifecycleEvent
}

function maybeCall(fn: unknown, context: unknown) {
  if (fn && typeof fn === 'function') {
    fn(context)
  }
}

export function useLifecycleEvents(
  context: unknown,
  events: ComponentLifecycleEvents
) {
  useEffect(() => {
    maybeCall(events.onMount, context)
    return () => {
      maybeCall(events.onUnmount, context)
    }
  }, [])

  useEffect(() => {
    maybeCall(events.onUpdate, context)
  })
}
