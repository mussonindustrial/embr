import type { Values } from './shared'

type PreviewPositionListener = () => void

const previewPositions = new WeakMap<object, Values>()
const previewPositionListeners = new Set<PreviewPositionListener>()

export function previewPositionOf(component: object): Values | undefined {
  return previewPositions.get(component)
}

export function setPreviewPosition(component: object, position?: Values) {
  if (position == null) {
    previewPositions.delete(component)
  } else {
    previewPositions.set(component, position)
  }

  previewPositionListeners.forEach((listener) => listener())
}

export function onPreviewPosition(listener: PreviewPositionListener) {
  previewPositionListeners.add(listener)

  return () => {
    previewPositionListeners.delete(listener)
  }
}
