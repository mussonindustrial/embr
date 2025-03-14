import { useEffect, useRef } from 'react'
import { ComponentLifecycleEvents } from '@embr-js/perspective-client'

export default function useRefLifecycleEvents<T>(
  ref: T | null | undefined,
  events: ComponentLifecycleEvents
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
