import React from 'react'
import {
  DesignerComponentStore,
  RectilinearHandles,
  ResizeCallback,
  SelectionStore,
} from '@inductiveautomation/perspective-designer'

import type { ResizeHandle } from '@/api'
import { findDropZone, slotOptions } from './placement'

const ALL_HANDLES: ResizeHandle[] = ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw']
const VIEW_HANDLES: ResizeHandle[] = ['e', 'se', 's']

function findDesignerHandle(handle: ResizeHandle) {
  const components = {
    n: RectilinearHandles.North,
    ne: RectilinearHandles.NorthEast,
    e: RectilinearHandles.East,
    se: RectilinearHandles.SouthEast,
    s: RectilinearHandles.South,
    sw: RectilinearHandles.SouthWest,
    w: RectilinearHandles.West,
    nw: RectilinearHandles.NorthWest,
  }

  return components[handle]
}

function getChildHandles(selection: SelectionStore): ResizeHandle[] {
  const [first] = selection.selectedComponents
  const container = first?.parent as DesignerComponentStore | undefined

  if (container == null) {
    return []
  }

  const zone = findDropZone(container, first)
  const resize = zone == null ? undefined : slotOptions.get(zone)?.resize

  if (!resize) {
    return []
  }

  return resize === true ? ALL_HANDLES : resize
}

function getHandles(selection: SelectionStore): ResizeHandle[] {
  const selected = selection.selectedComponents
  const isRootSelected =
    selected.length === 1 && selection.isRootContainer(selected[0].path)

  if (isRootSelected || selection.isViewSelected) {
    return VIEW_HANDLES
  }

  return getChildHandles(selection)
}

export function createHandles(
  selection: SelectionStore,
  resize: ResizeCallback
) {
  return getHandles(selection).map((handle) => {
    const Handle = findDesignerHandle(handle)

    return <Handle key={handle} select={selection} resize={resize} />
  })
}
