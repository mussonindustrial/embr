import type { Point } from '@inductiveautomation/perspective-common'
import type { DesignerComponentStore } from '@inductiveautomation/perspective-designer'

import type { Bounds, PositionChange, ResizeHandle, Values } from '@/api'

export type SlotOptions = {
  positionChild?: (change: PositionChange) => Values
  resize?: boolean | ResizeHandle[]
}

type PlacedSibling = { index: number; box: DOMRect }

export const DROP_ZONE = 'data-embr-drop-zone'
export const PLACEHOLDER = 'data-embr-placeholder'
export const DROP_ACTIVE = 'data-embr-drop-active'

const GRID_LINE = /^-?\d+$/
const SIZES = ['width', 'height'] as const

export const slotOptions = new WeakMap<Element, SlotOptions>()

let activeDropZone: Element | null = null

export function highlightDropZone(zone: Element | null) {
  if (activeDropZone === zone) {
    return
  }

  activeDropZone?.removeAttribute(DROP_ACTIVE)
  zone?.setAttribute(DROP_ACTIVE, '')
  activeDropZone = zone
}

export function elementOf(component: DesignerComponentStore): Element | null {
  return component.element ?? null
}

function isOwnedBy(root: Element | null, zone: Element): boolean {
  return root != null && zone.closest('[data-component-path]') === root
}

export function dropZoneAt(
  container: DesignerComponentStore,
  point: Point
): Element | null {
  const root = elementOf(container)

  if (root == null) {
    return null
  }

  const elementsUnder = document.elementsFromPoint(point.x, point.y)
  const zoneUnder =
    elementsUnder.find(
      (element) => element.hasAttribute(DROP_ZONE) && isOwnedBy(root, element)
    ) ?? null

  if (zoneUnder != null || !elementsUnder.includes(root)) {
    return zoneUnder
  }

  const zones = Array.from(root.querySelectorAll(`[${DROP_ZONE}]`))

  if (zones.some((zone) => isOwnedBy(root, zone))) {
    return null
  }

  const [firstChild] = container.children as DesignerComponentStore[]
  const firstElement = firstChild == null ? null : elementOf(firstChild)

  return firstElement?.parentElement ?? root
}

export function dropZoneOf(
  container: DesignerComponentStore,
  component: DesignerComponentStore
): Element | null {
  const root = elementOf(container)
  let zone = elementOf(component)?.parentElement?.closest(`[${DROP_ZONE}]`)

  while (zone != null && !isOwnedBy(root, zone)) {
    zone = zone.parentElement?.closest(`[${DROP_ZONE}]`)
  }

  return zone ?? null
}

function declaredPosition(zone: Element | null): Values {
  try {
    return JSON.parse(zone?.getAttribute(DROP_ZONE) ?? '{}')
  } catch {
    return {}
  }
}

export function scaleOf(zone: Element): number {
  const width = (zone as HTMLElement).offsetWidth
  return width > 0 ? zone.getBoundingClientRect().width / width : 1
}

function boxInZone(zone: Element, client: Bounds): Bounds {
  const element = zone as HTMLElement
  const frame = zone.getBoundingClientRect()
  const scale = scaleOf(zone)

  const x = (client.x - frame.left) / scale - element.clientLeft
  const y = (client.y - frame.top) / scale - element.clientTop

  return {
    x: Math.round(x + element.scrollLeft),
    y: Math.round(y + element.scrollTop),
    width: Math.round(client.width / scale),
    height: Math.round(client.height / scale),
  }
}

export function clientBoxOf(component: DesignerComponentStore): Bounds | null {
  const rect = elementOf(component)?.getBoundingClientRect()

  if (rect == null) {
    return null
  }

  return { x: rect.left, y: rect.top, width: rect.width, height: rect.height }
}

export function clientBoxesOf(
  components: DesignerComponentStore[]
): Map<DesignerComponentStore, Bounds> {
  return new Map(
    components.flatMap((component): [DesignerComponentStore, Bounds][] => {
      const box = clientBoxOf(component)
      return box == null ? [] : [[component, box]]
    })
  )
}

export function isInFlow(zone: Element, child?: Element | null): boolean {
  if (child == null) {
    return true
  }

  const { position, gridColumnStart, gridRowStart } = getComputedStyle(child)

  if (position === 'absolute' || position === 'fixed') {
    return false
  }

  const isGridPlaced =
    GRID_LINE.test(gridColumnStart) || GRID_LINE.test(gridRowStart)

  return !(isGridPlaced && getComputedStyle(zone).display.includes('grid'))
}

function isRowLayout(zone: Element, boxes: DOMRect[]): boolean {
  const { display, flexDirection } = getComputedStyle(zone)

  if (display.includes('flex')) {
    return !flexDirection.startsWith('column')
  }

  return boxes.some((box, index) => {
    const next = boxes[index + 1]
    return next != null && next.top < box.bottom && next.left >= box.right - 1
  })
}

function isBefore(point: Point, box: DOMRect, isRow: boolean): boolean {
  if (!isRow) {
    return point.y < box.top + box.height / 2
  }

  const isAbove = point.y < box.top
  const isLeftOfMiddle =
    point.y <= box.bottom && point.x < box.left + box.width / 2

  return isAbove || isLeftOfMiddle
}

export function dropIndexAt(
  zone: Element,
  siblings: DesignerComponentStore[],
  point: Point
): number {
  const placed = siblings.flatMap((sibling, index): PlacedSibling[] => {
    const element = elementOf(sibling)

    if (element == null || !zone.contains(element)) {
      return []
    }

    return [{ index, box: element.getBoundingClientRect() }]
  })

  if (placed.length === 0) {
    return siblings.length
  }

  const isRow = isRowLayout(
    zone,
    placed.map(({ box }) => box)
  )
  const next = placed.find(({ box }) => isBefore(point, box, isRow))

  return next?.index ?? placed[placed.length - 1].index + 1
}

function withoutUnchangedSize(
  position: Values,
  bounds: Bounds,
  reason: PositionChange['reason']
): Values {
  if (reason === 'resize') {
    return position
  }

  const kept = { ...position }

  SIZES.forEach((name) => {
    if (kept[name] === bounds[name]) {
      delete kept[name]
    }
  })

  return kept
}

export function childPositionAt(
  zone: Element | null,
  client: Bounds | null,
  reason: PositionChange['reason']
): Values {
  const declared = declaredPosition(zone)
  const positionChild =
    zone == null ? undefined : slotOptions.get(zone)?.positionChild

  if (positionChild == null || zone == null || client == null) {
    return declared
  }

  const element = zone as HTMLElement
  const bounds = boxInZone(zone, client)

  try {
    const position = positionChild({
      bounds,
      slot: { width: element.clientWidth, height: element.clientHeight },
      reason,
    })

    return { ...declared, ...withoutUnchangedSize(position, bounds, reason) }
  } catch (error) {
    console.warn('Cannot position child, positionChild threw an error.', error)
    return declared
  }
}

export function writePosition(
  component: DesignerComponentStore,
  position: Values
) {
  Object.entries(position).forEach(([name, value]) => {
    if (component.position.read(name) !== value) {
      component.position.write(name, value)
    }
  })
}
