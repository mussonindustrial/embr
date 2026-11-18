import {
  ComponentDefinition,
  ComponentInstanceDef,
  ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import type {
  DetailedRectangle,
  Point,
} from '@inductiveautomation/perspective-client'
import {
  ContainerDesignDelegate,
  DeepSelection,
  DesignerComponentStore,
  DesignerStore,
  Move,
  PreferredLocation,
  SelectionStore,
} from '@inductiveautomation/perspective-designer'

import type { LibraryComponent } from '@/api/periscopeApi'
import { createHandles } from './handles'
import {
  Box,
  clientBoxesOf,
  clientBoxOf,
  dropIndexAt,
  dropZoneAt,
  dropZoneOf,
  elementOf,
  highlightDropZone,
  isInFlow,
  childPositionAt,
  scaleOf,
  writePosition,
} from './placement'
import { previewPositionOf, setPreviewPosition } from './positionPreviews'
import { findLibraryComponent, Values } from './shared'

type PositionProps = LibraryComponent['periscope']['position']

type Gesture = {
  components: DesignerComponentStore[]
  boxes: Map<DesignerComponentStore, Box>
  indexes: number[]
}

type Resize = {
  key: string
  boxes: Map<DesignerComponentStore, Box>
}

const RESELECT_DELAYS = [0, 100]

function restoreSelection(
  selection: SelectionStore,
  components: DesignerComponentStore[]
) {
  const paths = components.map((component) => component.path)

  const restore = () => {
    const current = selection.selectedComponents.map(
      (component) => component.path
    )

    if (current.join() !== paths.join()) {
      selection.selectComponents(paths)
    }
  }

  for (const delay of RESELECT_DELAYS) {
    window.setTimeout(restore, delay)
  }
}

function refreshSelectionBounds(selection: SelectionStore) {
  window.requestAnimationFrame(() => {
    window.requestAnimationFrame(() =>
      selection.forceUpdateSelectionDimensions()
    )
  })
}

function childrenOf(container: DesignerComponentStore) {
  return container.children as DesignerComponentStore[]
}

function shiftBox(box: Box, x: number, y: number): Box {
  return { ...box, x: box.x + x, y: box.y + y }
}

function dropIndexOf(
  zone: Element | undefined,
  siblings: DesignerComponentStore[],
  location?: PreferredLocation
): number | undefined {
  if (zone == null || location == null) {
    return undefined
  }

  const siblingInZone = siblings
    .map(elementOf)
    .find((element) => element != null && zone.contains(element))

  if (!isInFlow(zone, siblingInZone)) {
    return undefined
  }

  return dropIndexAt(zone, siblings, location)
}

function newChildPosition(
  definition: ComponentDefinition,
  positionProps: PositionProps,
  zone: Element | undefined,
  location?: PreferredLocation
): Values {
  const positionNames = Object.keys(positionProps?.schema.properties ?? {})
  const keptPosition: Values = {}

  for (const [name, value] of Object.entries(definition.position ?? {})) {
    if (positionNames.includes(name)) {
      keptPosition[name] = value
    }
  }

  const size = ComponentRegistry.getMeta(definition.type)?.getDefaultSize()
  const scale = zone == null ? 1 : scaleOf(zone)

  let client: Box | undefined

  if (location != null) {
    const width = Number(location.width ?? size?.width) || 0
    const height = Number(location.height ?? size?.height) || 0

    client = {
      x: location.x,
      y: location.y,
      width: width * scale,
      height: height * scale,
    }
  }

  return {
    ...positionProps?.defaults,
    ...keptPosition,
    ...childPositionAt(zone, client, 'drop'),
  }
}

export class LibraryComponentDesignDelegate implements ContainerDesignDelegate {
  private gesture?: Gesture
  private resize?: Resize

  readonly isContainer = true

  constructor(readonly type: string) {}

  createHandles(selection: SelectionStore) {
    return createHandles(selection, this.applyResize)
  }

  addNewComponents(
    definitions: ComponentDefinition[],
    _selection: SelectionStore,
    container: DesignerComponentStore,
    location?: PreferredLocation
  ): ComponentInstanceDef[] {
    const positionProps = findLibraryComponent(this.type)?.periscope.position
    const zone = location == null ? undefined : dropZoneAt(container, location)
    const siblings = [...childrenOf(container)]
    const index = dropIndexOf(zone, siblings, location)

    const added = definitions.map((definition) => {
      const component = {
        ...definition,
        position: newChildPosition(definition, positionProps, zone, location),
      }

      return { component, addressPath: container._addComponent(component) }
    })

    if (index != null && index < siblings.length) {
      const paths = added.map(({ addressPath }) => addressPath)

      window.setTimeout(() => {
        const stores = childrenOf(container).filter((child) =>
          paths.includes(child.addressPathString)
        )
        container.reorderChildren(stores, index, false)
      }, 0)
    }

    return added
  }

  onMove(
    move: Move | undefined,
    selection: SelectionStore,
    _initialPositions: Map<string, unknown>,
    selected: DesignerComponentStore[],
    parent: DeepSelection
  ) {
    const container = parent.component
    const components = this.gesture?.components ?? selected
    const selectionDrifted =
      selected.length !== components.length ||
      selected.some((component, index) => component !== components[index])

    if (selectionDrifted) {
      selection.selectComponents(components.map((component) => component.path))
    }

    if (move == null) {
      this.endMove(container, components, selection)
    } else if (move.sourceEventType === 'key') {
      this.nudge(container, selected, move)
    } else {
      this.drag(container, components, selection, move)
    }
  }

  private endMove(
    container: DesignerComponentStore,
    components: DesignerComponentStore[],
    selection: SelectionStore
  ) {
    highlightDropZone()

    const initialIndexes = this.gesture?.indexes
    this.gesture = undefined

    for (const component of components) {
      const position = previewPositionOf(component)

      if (position != null) {
        setPreviewPosition(component)
        writePosition(component, position)
      }
    }

    const currentIndexes = container.childrenIndexes
    const wasReordered =
      initialIndexes != null && initialIndexes.join() !== currentIndexes.join()

    if (wasReordered) {
      const designer = container.view.page.parent as unknown as DesignerStore

      designer.onUndoableAction('undo-reorder-children', {
        path: container.addressPathString,
        previousChildrenIndexes: initialIndexes,
        newChildrenIndexes: currentIndexes,
      })
      restoreSelection(selection, components)
    }

    refreshSelectionBounds(selection)
  }

  private nudge(
    container: DesignerComponentStore,
    selected: DesignerComponentStore[],
    move: Move
  ) {
    for (const component of selected) {
      const zone = dropZoneOf(container, component)
      const box = clientBoxOf(component)

      if (zone == null || box == null) {
        continue
      }

      const scale = scaleOf(zone)
      const moved = shiftBox(box, move.delta.x * scale, move.delta.y * scale)

      writePosition(component, childPositionAt(zone, moved, 'move'))
    }
  }

  private drag(
    container: DesignerComponentStore,
    components: DesignerComponentStore[],
    selection: SelectionStore,
    move: Move
  ) {
    this.gesture ??= {
      components,
      boxes: clientBoxesOf(components),
      indexes: [...container.childrenIndexes],
    }

    const { boxes } = this.gesture
    const { origin, target } = move

    const zone = dropZoneAt(container, target)
    highlightDropZone(zone)

    if (zone == null) {
      return
    }

    for (const component of components) {
      const start = boxes.get(component)
      const client =
        start == null
          ? undefined
          : shiftBox(start, target.x - origin.x, target.y - origin.y)

      const position = childPositionAt(zone, client, 'move')
      const current = JSON.stringify(previewPositionOf(component))

      if (current !== JSON.stringify(position)) {
        setPreviewPosition(component, position)
      }
    }

    refreshSelectionBounds(selection)

    if (isInFlow(zone, elementOf(components[0]))) {
      this.reorder(container, components, zone, target)
    }
  }

  private reorder(
    container: DesignerComponentStore,
    components: DesignerComponentStore[],
    zone: Element,
    target: Point
  ) {
    const children = childrenOf(container)
    const siblings = children.filter((child) => !components.includes(child))
    const index = dropIndexAt(zone, siblings, target)

    const first = children.indexOf(components[0])
    const currentRun = children.slice(first, first + components.length)
    const isInPlace =
      first === index &&
      currentRun.every((child, offset) => child === components[offset])

    if (!isInPlace) {
      container.reorderChildren(components, index, false)
    }
  }

  applyResize = (
    originals: Map<string, DetailedRectangle>,
    bounds: Map<string, DetailedRectangle>,
    components: DesignerComponentStore[],
    move?: Move
  ) => {
    const container = components[0]?.parent as
      | DesignerComponentStore
      | undefined

    if (move == null || container == null) {
      this.resize = undefined
      return
    }

    const key = JSON.stringify(move.origin)

    if (this.resize?.key !== key) {
      this.resize = { key, boxes: clientBoxesOf(components) }
    }

    const { boxes } = this.resize

    for (const component of components) {
      const zone = dropZoneOf(container, component)
      const start = boxes.get(component)
      const original = originals.get(component.path)
      const next = bounds.get(component.path)

      if (zone == null || start == null || original == null || next == null) {
        continue
      }

      const scale = original.width > 0 ? start.width / original.width : 1
      const client = {
        x: start.x + (next.x - original.x) * scale,
        y: start.y + (next.y - original.y) * scale,
        width: next.width * scale,
        height: next.height * scale,
      }

      writePosition(component, childPositionAt(zone, client, 'resize'))
    }
  }
}
