import {
  ComponentStore,
  EventGroupActions,
  StandardEventGroup,
  PlainObject,
} from '@inductiveautomation/perspective-client'

type PeriscopeComponentEvents =
  | 'lifecycle.onMount'
  | 'lifecycle.onUnmount'
  | 'visibility.onVisible'
  | 'visibility.onHidden'
  | 'page.onPageVisible'
  | 'page.onPageHidden'

/**
 * Periscope events are stored as namespaced `system` events
 * (`embr.periscope.lifecycle.onMount`), which Perspective already saves, sends to the
 * browser, and builds actions for. The Designer shows them by short name.
 */
export const NAMESPACE = 'embr.periscope'

const INSTALLED = Symbol.for('embr.periscope.componentEvents')
const runningWatchers = new WeakMap<ComponentStore, WatcherDisposer[]>()
const mountedElements = new WeakMap<ComponentStore, Element>()
const watchers: Watcher[] = [
  {
    events: ['visibility.onVisible', 'visibility.onHidden'],
    start: watchVisibility,
  },
  {
    events: ['page.onPageVisible', 'page.onPageHidden'],
    start: watchPageVisibility,
  },
]

type ComponentEventHandler = (
  eventName: PeriscopeComponentEvents,
  event: PlainObject
) => void

type RefCallback = (element: Element | null) => void
type WatcherDisposer = () => void
type Watcher = {
  events: PeriscopeComponentEvents[]
  start: (element: Element, fire: ComponentEventHandler) => WatcherDisposer
}

function eventKey(eventName: PeriscopeComponentEvents) {
  return `${NAMESPACE}.${eventName}`
}

function shouldNotifyGateway(store: ComponentStore, key: string): boolean {
  const events = store.componentEvents
  const actions: EventGroupActions | undefined =
    events['systemEventActions']?.[key]

  return (
    !!actions?.gatewayActionRequired &&
    events['eventsEnabled']() &&
    store.view.page.parent.areActionsEligible
  )
}

function fireLifecycleEvent(
  store: ComponentStore,
  eventName: PeriscopeComponentEvents,
  event: PlainObject
) {
  const key = eventKey(eventName)

  store.componentEvents.runClientActions(StandardEventGroup.SYSTEM, key, event)

  if (shouldNotifyGateway(store, key)) {
    store.onEventFired(StandardEventGroup.SYSTEM, key, event)
  }
}

function safelyFire(
  store: ComponentStore,
  eventName: PeriscopeComponentEvents,
  event: PlainObject
) {
  try {
    fireLifecycleEvent(store, eventName, event)
  } catch (error) {
    console.error(`Error firing ${eventKey(eventName)}`, error)
  }
}

function watchVisibility(
  element: Element,
  fire: ComponentEventHandler
): WatcherDisposer {
  const observer = new IntersectionObserver(([entry]) => {
    const eventName = entry.isIntersecting
      ? 'visibility.onVisible'
      : 'visibility.onHidden'
    fire(eventName, { ratio: entry.intersectionRatio })
  })

  observer.observe(element)
  return () => observer.disconnect()
}

function watchPageVisibility(
  _element: Element,
  fire: ComponentEventHandler
): WatcherDisposer {
  const onChange = () => {
    const eventName = document.hidden
      ? 'page.onPageHidden'
      : 'page.onPageVisible'
    fire(eventName, {})
  }

  document.addEventListener('visibilitychange', onChange)
  return () => document.removeEventListener('visibilitychange', onChange)
}

function hasActionsFor(
  store: ComponentStore,
  events: PeriscopeComponentEvents[]
) {
  const system = store.def.events?.system
  return events.some((eventName) => system?.[eventKey(eventName)] != null)
}

function startWatchers(store: ComponentStore, element: Element) {
  stopWatchers(store) // never orphan a previous set

  const fire: ComponentEventHandler = (eventName, event) =>
    safelyFire(store, eventName, event)

  const stops = watchers
    .filter((watcher) => hasActionsFor(store, watcher.events))
    .map((watcher) => watcher.start(element, fire))

  runningWatchers.set(store, stops)
}

function stopWatchers(store: ComponentStore) {
  runningWatchers.get(store)?.forEach((stop) => stop())
  runningWatchers.delete(store)
}

function withLifecycleEvents(
  store: ComponentStore,
  original: RefCallback
): RefCallback {
  return (element) => {
    const previous = mountedElements.get(store)
    const isUnmounting = previous != null && previous !== element
    const isMounting = element != null && previous !== element

    if (isUnmounting) {
      stopWatchers(store)
      mountedElements.delete(store)
      safelyFire(store, 'lifecycle.onUnmount', {})
    }

    original(element)

    if (isMounting) {
      mountedElements.set(store, element)
      safelyFire(store, 'lifecycle.onMount', {})
      startWatchers(store, element)
    }
  }
}

export function installLifecycleEvents() {
  const proto = ComponentStore.prototype as ComponentStore & {
    [INSTALLED]?: boolean
  }
  if (proto[INSTALLED]) return

  const descriptor = Object.getOwnPropertyDescriptor(proto, 'refCallback')
  const getBoundRefCallback = descriptor?.get
  if (!getBoundRefCallback) {
    console.warn(`${NAMESPACE}: unable to install Periscope component events.`)
    return
  }

  Object.defineProperty(proto, 'refCallback', {
    configurable: true,
    get(this: ComponentStore) {
      const original = getBoundRefCallback.call(this) as RefCallback
      const refCallback = withLifecycleEvents(this, original)
      Object.assign(this, { refCallback })
      return refCallback
    },
  })

  Object.defineProperty(proto, INSTALLED, { value: true })
}
