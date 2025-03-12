import { ComponentDomEvents } from './useDomEvents'
import { ComponentLifecycleEvents } from './useLifecycleEvents'

export * from './useDomEvents'
export * from './useLifecycleEvents'
export * from './useComponentEvents'

export type ComponentEvents = {
  dom?: ComponentDomEvents
  lifecycle?: ComponentLifecycleEvents
}
