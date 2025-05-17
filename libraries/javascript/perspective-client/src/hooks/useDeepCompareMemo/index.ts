import { DependencyList, useRef } from 'react'
import { useFirstMountState } from '../useFirstMountState'
import { useDeepCompareEffect } from '../useDeepCompareEffect'

export function useDeepCompareMemo<T>(factory: () => T, deps: DependencyList) {
  const firstMount = useFirstMountState()
  const value = useRef<T>()

  if (value.current === undefined) {
    value.current = factory()
  }

  useDeepCompareEffect(() => {
    if (!firstMount) {
      value.current = factory()
    }
  }, deps)

  return value.current as T
}
