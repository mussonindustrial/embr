import { SyntheticEvent, useEffect } from 'react'
import { ComponentStore } from '@inductiveautomation/perspective-client'

export type DomEvent = (e: SyntheticEvent<any>) => void

const DOM_EVENTS = [
  'onAnimationStart',
  'onAnimationIteration',
  'onAnimationEnd',
  'onCopy',
  'onCut',
  'onPaste',
  'onCompositionStart',
  'onCompositionUpdate',
  'onCompositionEnd',
  'onDragStart',
  'onDragEnd',
  'onDragEnter',
  'onDragLeave',
  'onDragOver',
  'onDrop',
  'onFocus',
  'onBlur',
  'onBeforeInput',
  'onKeyDown',
  'onKeyUp',
  'onClick',
  'onMouseEnter',
  'onMouseOver',
  'onMouseDown',
  'onMouseUp',
  'onMouseLeave',
  'onPointerEnter',
  'onPointerMove',
  'onPointerDown',
  'onPointerUp',
  'onPointerLeave',
  'onTouchStart',
  'onTouchMove',
  'onTouchEnd',
  'onTouchCancel',
  'onTransitionEnd',
  'onScroll',
  'onWheel',
] as const
export type DomEvents = {
  [K in (typeof DOM_EVENTS)[number]]?: DomEvent
}

export function useDomEvents(
  componentStore: ComponentStore,
  events: DomEvents
) {
  useEffect(() => {
    const disposers = DOM_EVENTS.map((eventName) => {
      if (events[eventName] && typeof events[eventName] === 'function') {
        return componentStore.domEvents.addListener(
          eventName,
          events[eventName]
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
