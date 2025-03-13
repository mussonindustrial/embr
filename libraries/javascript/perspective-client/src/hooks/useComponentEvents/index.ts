import { useLifecycleEvents } from '../useLifecycleEvents'
import { useDomEvents } from '../useDomEvents'
import { ComponentStore } from '@inductiveautomation/perspective-client'
import { ComponentEvents } from '../index'

export function useComponentEvents(
  store: ComponentStore,
  events: ComponentEvents,
  context: any
) {
  useLifecycleEvents(context, events?.lifecycle ?? {})
  useDomEvents(store, events?.dom ?? {})
}
