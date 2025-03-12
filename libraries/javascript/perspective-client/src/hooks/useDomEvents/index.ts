import { DOMAttributes, SyntheticEvent, useEffect } from 'react'
import { ComponentStore } from '@inductiveautomation/perspective-client'

export type ComponentDomEvent = (e: SyntheticEvent<any>) => void
export type ComponentDomEvents = Omit<
  DOMAttributes<any>,
  'children' | 'dangerouslySetInnerHTML'
>

export function useDomEvents(
  componentStore: ComponentStore,
  events: ComponentDomEvents
) {
  useEffect(() => {
    const assignedEvents = Object.keys(events) as (keyof typeof events)[]
    const disposers = assignedEvents.map((eventName) => {
      if (events[eventName] && typeof events[eventName] === 'function') {
        return componentStore.domEvents.addListener(
          eventName,
          events[eventName] as ComponentDomEvent
        )
      }
      return undefined
    })

    return () => {
      disposers.forEach((disposer) => {
        disposer?.()
      })
    }
  }, [events])
}
