import { useEffect } from 'react'

export type LifecycleEvent = (context?: unknown) => void

export type LifecycleEvents = {
  onMount?: LifecycleEvent
  onUpdate?: LifecycleEvent
  onUnmount?: LifecycleEvent

  /** @deprecated use onRender */
  beforeRender?: LifecycleEvent
}

function maybeCall(fn: unknown, context: unknown) {
  if (fn && typeof fn === 'function') {
    fn(context)
  }
}

export function useLifecycleEvents(context: unknown, events: LifecycleEvents) {
  maybeCall(events.beforeRender, context)
  maybeCall(events.onUpdate, context)

  useEffect(() => {
    maybeCall(events.onMount, context)
    return () => {
      maybeCall(events.onUnmount, context)
    }
  }, [])
}
