import type { MutableRefObject, Ref } from 'react'

export default function assignRef<T>(ref: Ref<T> | undefined, value: T | null) {
  if (typeof ref === 'function') {
    ref(value)
  } else if (ref != null) {
    ;(ref as MutableRefObject<T | null>).current = value
  }
}
