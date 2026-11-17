import React, {
  ElementType,
  forwardRef,
  useCallback,
  useEffect,
  useRef,
} from 'react'
import {
  ComponentStore,
  layoutCallbackCreator,
} from '@inductiveautomation/perspective-client'

import { slotChildren, useComponent } from '@/api/context'
import type { Child, ChildSlotProps } from '@/api/periscopeApi'
import { replaceEqualDeep } from '@/api/props'
import { PLACEHOLDER, DROP_ZONE, SlotOptions, slotOptions } from './placement'
import { onPreviewPosition, previewPositionOf } from './positionPreviews'
import {
  assignRef,
  isDesigning,
  isPreviewing,
  selectedPaths,
  splitClasses,
  useForceUpdate,
  Values,
} from './shared'

export type Children = { stores: ComponentStore[]; handles: Child[] }

type ChildEntry = {
  store: ComponentStore
  position: Values
  child: Child
}

type Mobx = {
  reaction(read: () => unknown, effect: () => void): () => void
}

const NO_CHILDREN: Children = { stores: [], handles: [] }

export const ChildSlot = forwardRef<HTMLElement, ChildSlotProps>(
  function ChildSlot(
    {
      as,
      where,
      getPosition,
      resize,
      placeholder,
      children: renderChild,
      ...attributes
    },
    ref
  ) {
    const { children, mode } = useComponent()
    const element = useRef<HTMLElement | null>(null)

    const attachElement = useCallback(
      (node: HTMLElement | null) => {
        element.current = node
        assignRef(ref, node)
      },
      [ref]
    )

    useEffect(() => {
      if (element.current == null || mode === 'session') {
        return
      }

      slotOptions.set(element.current, {
        getPosition: getPosition as SlotOptions['getPosition'],
        resize,
      })
    })

    const Tag = (as ?? 'div') as ElementType
    const placedChildren = slotChildren(children, where, renderChild)
    const shownPlaceholder =
      mode === 'design' && placedChildren.length === 0 ? placeholder : undefined
    const dropZone =
      mode === 'session' ? {} : { [DROP_ZONE]: JSON.stringify(where ?? {}) }

    return (
      <Tag {...attributes} ref={attachElement} {...dropZone}>
        {placedChildren}
        {shownPlaceholder == null ? null : (
          <div {...{ [PLACEHOLDER]: '' }}>{shownPlaceholder}</div>
        )}
      </Tag>
    )
  }
)

function createChild(
  store: ComponentStore,
  index: number,
  position: Values
): Child {
  const key = store.addressPathString

  return {
    key,
    index,
    name: String(store.meta.read('name') ?? ''),
    type: store.def.type,
    position,
    selected: false,
    render: (options) => {
      const Component = store.getComponent()
      const layout = layoutCallbackCreator.forClassAndStyle(
        () => options?.style ?? {},
        splitClasses(options?.className)
      )

      return <Component key={key} layout={layout} />
    },
  }
}

function updateEntry(
  previous: ChildEntry | undefined,
  store: ComponentStore,
  index: number,
  defaults: Values
): ChildEntry {
  const isSameStore = previous?.store === store
  const position = replaceEqualDeep(
    isSameStore ? previous?.position : undefined,
    {
      ...defaults,
      ...(store.position.read() as Values),
      ...previewPositionOf(store),
    }
  )

  if (isSameStore && previous?.position === position) {
    return previous
  }

  return { store, position, child: createChild(store, index, position) }
}

function isSameEntries(next: ChildEntry[], previous: ChildEntry[]): boolean {
  if (next.length !== previous.length) {
    return false
  }

  return next.every((entry, index) => entry === previous[index])
}

export function useChildren(
  store: ComponentStore,
  defaults?: Values
): Children {
  const refresh = useForceUpdate()
  const entries = useRef<ChildEntry[]>([])
  const children = useRef<Children>(NO_CHILDREN)
  const stores = defaults == null ? [] : store.children
  const paths = stores.map((child) => child.addressPathString).join('|')

  useEffect(() => {
    const unsubscribes = stores.map((child) =>
      child.position.subscribe(refresh)
    )
    return () => unsubscribes.forEach((unsubscribe) => unsubscribe())
  }, [paths])

  useEffect(
    () => (defaults == null ? undefined : onPreviewPosition(refresh)),
    []
  )

  if (defaults == null) {
    return NO_CHILDREN
  }

  const nextEntries = stores.map((child, index) =>
    updateEntry(entries.current[index], child, index, defaults)
  )

  if (!isSameEntries(nextEntries, entries.current)) {
    entries.current = nextEntries
    children.current = {
      stores: nextEntries.map((entry) => entry.store),
      handles: nextEntries.map((entry) => entry.child),
    }
  }

  return children.current
}

function holdsSelection(path: string | undefined, selected: string[]) {
  if (path == null) {
    return false
  }

  return selected.some((item) => item === path || item.startsWith(`${path}:`))
}

export function useSelectedChild(children: Children): Child | undefined {
  const refresh = useForceUpdate()
  const isWatching = children.stores.length > 0 && isDesigning()

  useEffect(() => {
    const mobx = (window as unknown as { mobx?: Mobx }).mobx

    if (!isWatching || mobx == null) {
      return undefined
    }

    return mobx.reaction(() => selectedPaths().join('|'), refresh)
  }, [isWatching])

  if (!isWatching || isPreviewing()) {
    return undefined
  }

  const selected = selectedPaths()
  const index = children.stores.findIndex((store) =>
    holdsSelection(store.path, selected)
  )

  return children.handles[index]
}
