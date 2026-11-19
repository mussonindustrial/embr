import type { Point } from '@inductiveautomation/perspective-client'
import { DesignerComponentStore } from '@inductiveautomation/perspective-designer'

import type { PositionChange, ResizeHandle } from '@/api/periscopeApi'
import type { Values } from './shared'

export type Box = { x: number; y: number; width: number; height: number }

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

let activeDropZone: Element | undefined

export function highlightDropZone(zone?: Element) {
  if (activeDropZone === zone) {
    return
  }

  activeDropZone?.removeAttribute(DROP_ACTIVE)
  zone?.setAttribute(DROP_ACTIVE, '')
  activeDropZone = zone
}

export function elementOf(component: DesignerComponentStore) {
  return component.element ?? undefined
}

function isOwnedBy(root: Element | undefined, zone: Element): boolean {
  return zone.closest('[data-component-path]') === root
}

export function dropZoneAt(
  container: DesignerComponentStore,
  point: Point
): Element | undefined {
  const root = elementOf(container)

  if (root == null) {
    return undefined
  }

  const elementsUnder = document.elementsFromPoint(point.x, point.y)
  const zoneUnder = elementsUnder.find(
    (element) => element.hasAttribute(DROP_ZONE) && isOwnedBy(root, element)
  )

  if (zoneUnder != null || !elementsUnder.includes(root)) {
    return zoneUnder
  }

  const zones = Array.from(root.querySelectorAll(`[${DROP_ZONE}]`))

  if (zones.some((zone) => isOwnedBy(root, zone))) {
    return undefined
  }

  const [firstChild] = container.children as DesignerComponentStore[]
  const firstElement = firstChild == null ? undefined : elementOf(firstChild)

  return firstElement?.parentElement ?? root
}

export function dropZoneOf(
  container: DesignerComponentStore,
  component: DesignerComponentStore
): Element | undefined {
  const root = elementOf(container)
  let zone = elementOf(component)?.parentElement?.closest(`[${DROP_ZONE}]`)

  while (zone != null && !isOwnedBy(root, zone)) {
    zone = zone.parentElement?.closest(`[${DROP_ZONE}]`)
  }

  return zone ?? undefined
}

function declaredPosition(zone: Element | undefined): Values {
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

function boxInZone(zone: Element, client: Box): Box {
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

export function clientBoxOf(
  component: DesignerComponentStore
): Box | undefined {
  const rect = elementOf(component)?.getBoundingClientRect()

  if (rect == null) {
    return undefined
  }

  return { x: rect.left, y: rect.top, width: rect.width, height: rect.height }
}

export function clientBoxesOf(
  components: DesignerComponentStore[]
): Map<DesignerComponentStore, Box> {
  const boxes = new Map<DesignerComponentStore, Box>()

  for (const component of components) {
    const box = clientBoxOf(component)

    if (box != null) {
      boxes.set(component, box)
    }
  }

  return boxes
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
  const placed: PlacedSibling[] = []

  siblings.forEach((sibling, index) => {
    const element = elementOf(sibling)

    if (element != null && zone.contains(element)) {
      placed.push({ index, box: element.getBoundingClientRect() })
    }
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
  bounds: Box,
  reason: PositionChange['reason']
): Values {
  if (reason === 'resize') {
    return position
  }

  const kept = { ...position }

  for (const name of SIZES) {
    if (kept[name] === bounds[name]) {
      delete kept[name]
    }
  }

  return kept
}

export function childPositionAt(
  zone: Element | undefined,
  client: Box | undefined,
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
    console.error('Error positioning child', error)
    return declared
  }
}

export function writePosition(
  component: DesignerComponentStore,
  position: Values
) {
  for (const [name, value] of Object.entries(position)) {
    if (component.position.read(name) !== value) {
      component.position.write(name, value)
    }
  }
}
