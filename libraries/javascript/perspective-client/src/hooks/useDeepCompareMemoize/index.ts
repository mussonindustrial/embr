import React from 'react'
import isDeepEqualReact from 'fast-deep-equal/react'

export function useDeepCompareMemoize(dependencies: React.DependencyList) {
  const dependenciesRef = React.useRef<React.DependencyList>(dependencies)
  const signalRef = React.useRef<number>(0)

  if (!isDeepEqualReact(dependencies, dependenciesRef.current)) {
    dependenciesRef.current = dependencies
    signalRef.current += 1
  }

  return React.useMemo(() => dependenciesRef.current, [signalRef.current])
}
