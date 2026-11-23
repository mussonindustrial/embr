function isRefObject(target: unknown): target is { current: unknown } {
  return (
    target != null &&
    typeof target === 'object' &&
    Object.keys(target).length === 1 &&
    'current' in target
  )
}

/** What `this` is in proxied JavaScript: the target, a ref's current value, or the Root element. */
export function proxyTargetOf(
  target: unknown,
  element: Element | null
): object | undefined {
  const value = isRefObject(target) ? target.current : target
  return (value ?? element ?? undefined) as object | undefined
}
