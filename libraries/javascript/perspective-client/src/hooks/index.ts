import { ComponentDomEvents } from './useDomEvents'
import { ComponentLifecycleEvents } from './useLifecycleEvents'

export * from './useComponentEvents'
export * from './useDeepCompareCallback'
export * from './useDeepCompareEffect'
export * from './useDeepCompareLayoutEffect'
export * from './useDeepCompareMemo'
export * from './useDomEvents'
export * from './useFirstMountState'
export * from './useLifecycleEvents'
export * from './useRefLifecycleEvents'

export type ComponentEvents = {
  dom?: ComponentDomEvents
  lifecycle?: ComponentLifecycleEvents
}
