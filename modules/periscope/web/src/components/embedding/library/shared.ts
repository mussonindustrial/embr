import { useReducer } from 'react'
import type { HTMLAttributes, Ref } from 'react'
import type { ComponentProps } from '@inductiveautomation/perspective-client'
import {
  type ComponentEvents,
  getDesignerStore,
} from '@embr-js/perspective-client'

import {
  COMPONENT_TYPE_PREFIX,
  type ComponentMode,
  type LibraryComponent,
} from '@/api/periscopeApi'
import type { RootOptions } from '@/api/root'

export type Values = Record<string, unknown>
export type Perspective = ComponentProps<Values & { events?: ComponentEvents }>

type RootRef = (element: HTMLElement | null) => void
type ElementRef = NonNullable<Ref<HTMLElement>>
type RootAttributes = HTMLAttributes<HTMLElement> & { ref?: RootRef }

type DesignerState = {
  isPreviewing?: boolean
  selection?: { selectedComponents: { path?: string }[] }
}

const mergedRefs = new WeakMap<object, WeakMap<object, RootRef>>()

export function findLibraryComponent(
  type: string
): LibraryComponent | undefined {
  const key = type.slice(COMPONENT_TYPE_PREFIX.length)
  return Embr?.clientResources?.components.get(key)
}

export function splitClasses(className?: string): string[] {
  return className?.split(/\s+/).filter(Boolean) ?? []
}

export function assignRef(
  ref: Ref<HTMLElement> | undefined,
  element: HTMLElement | null
) {
  if (typeof ref === 'function') {
    ref(element)
    return
  }

  if (ref != null) {
    const objectRef = ref as { current: HTMLElement | null }
    objectRef.current = element
  }
}

function mergeRefs(ownRef: ElementRef, givenRef: ElementRef): RootRef {
  const byGivenRef = mergedRefs.get(ownRef) ?? new WeakMap<object, RootRef>()
  mergedRefs.set(ownRef, byGivenRef)

  let merged = byGivenRef.get(givenRef)

  if (merged == null) {
    merged = (element: HTMLElement | null) => {
      assignRef(ownRef, element)
      assignRef(givenRef, element)
    }
    byGivenRef.set(givenRef, merged)
  }

  return merged
}

export function rootAttributes(
  perspective: Perspective,
  options?: RootOptions
): RootAttributes {
  const emitted = perspective.emit({
    classes: splitClasses(options?.className),
    style: options?.style ?? {},
  }) as HTMLAttributes<HTMLElement> & { ref?: Ref<HTMLElement> }

  const { ref: ownRef, ...attributes } = emitted
  const givenRef = options?.ref

  if (ownRef == null && givenRef == null) {
    return attributes
  }

  if (ownRef == null || givenRef == null) {
    const ref = (ownRef ?? givenRef) as unknown as RootRef
    return { ...attributes, ref }
  }

  return { ...attributes, ref: mergeRefs(ownRef, givenRef) }
}

function designerState() {
  return getDesignerStore() as DesignerState | undefined
}

export function isDesigning(): boolean {
  return designerState() != null
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

export function useForceUpdate(): () => void {
  const [, refresh] = useReducer((count: number) => count + 1, 0)
  return refresh
}
