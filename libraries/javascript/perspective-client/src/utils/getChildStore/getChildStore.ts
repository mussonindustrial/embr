import { AbstractUIElementStore } from '@inductiveautomation/perspective-client'

/**
 * Get a child store given the address path.
 * @param store
 * @param path
 */
export default function getChildStore(
  store: AbstractUIElementStore | undefined,
  path: number[]
): AbstractUIElementStore | undefined {
  let current = store

  for (const index of path) {
    if (!current?.children || current.children[index] === undefined) {
      // Path cannot be fully resolved
      return undefined
    }
    current = current.children[index]
  }

  return current
}
