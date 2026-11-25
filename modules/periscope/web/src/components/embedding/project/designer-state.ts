import { getClientStore, getDesignerStore } from '@embr-js/perspective-client'

import type { ComponentMode } from '@/api'

type DesignerState = {
  isPreviewing?: boolean
  selection?: { selectedComponents: { path?: string }[] }
}

function designerState() {
  return getDesignerStore() as DesignerState | undefined
}

export function isDesigning(): boolean {
  return getClientStore()?.isDesigner === true
}

export function isPreviewing(): boolean {
  return designerState()?.isPreviewing === true
}

export function currentMode(): ComponentMode {
  if (!isDesigning()) {
    return 'session'
  }

  return isPreviewing() ? 'preview' : 'design'
}

export function selectedPaths(): string[] {
  const selected = designerState()?.selection?.selectedComponents ?? []
  return selected.map((component) => component.path ?? '')
}
