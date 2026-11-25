import type { HTMLAttributes, Ref } from 'react'
import { assignRef, uniqueClasses } from '@embr-js/utils'

import type { RootOptions } from '@/api'
import { currentMode } from './designer-state'
import type { PerspectiveProps } from './types'

export const ANIMATIONS_PAUSED = 'data-embr-animations-paused'

type RootRef = (element: HTMLElement | null) => void
type ElementRef = NonNullable<Ref<HTMLElement>>
type RootAttributes = HTMLAttributes<HTMLElement> & {
  ref?: RootRef
  [ANIMATIONS_PAUSED]?: string
}

const mergedRefs = new WeakMap<object, WeakMap<object, RootRef>>()

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
  perspective: PerspectiveProps,
  options?: RootOptions
): RootAttributes {
  const emitted = perspective.emit({
    classes: uniqueClasses(options?.className),
    style: options?.style ?? {},
  }) as HTMLAttributes<HTMLElement> & { ref?: Ref<HTMLElement> }

  const { ref: ownRef, ...emittedAttributes } = emitted
  const attributes: RootAttributes =
    currentMode() === 'design'
      ? { ...emittedAttributes, [ANIMATIONS_PAUSED]: '' }
      : emittedAttributes
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
