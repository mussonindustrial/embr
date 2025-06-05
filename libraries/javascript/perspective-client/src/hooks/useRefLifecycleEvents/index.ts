import { useEffect, useRef } from 'react'
import { ComponentLifecycleEvents } from '../useLifecycleEvents'

export function useRefLifecycleEvents<T>(
  events: ComponentLifecycleEvents,
  ref: T | null | undefined
) {
  const mounted = useRef(false)

  useEffect(() => {
    if (ref != null && !mounted.current) {
      events.onMount?.(ref)
      mounted.current = true
    }

    return () => {
      if (ref != null && mounted.current) {
        events.onUnmount?.(ref)
        mounted.current = false
      }
    }
  }, [ref])

  if (ref != null && mounted.current) {
    events.onUpdate?.(ref)
  }
}
