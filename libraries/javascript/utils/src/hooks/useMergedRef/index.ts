import { Ref, useMemo } from 'react'
import { assignRef } from '../../utils'

export function useMergedRef<T>(...refs: (Ref<T> | undefined)[]) {
  return useMemo(() => {
    const defined = refs.filter((ref) => ref != null)

    if (defined.length < 2) {
      return defined[0]
    }

    return (value: T | null) => defined.forEach((ref) => assignRef(ref, value))
  }, refs)
}
