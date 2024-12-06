import { ClientStore } from '@inductiveautomation/perspective-client'

/**
 * Get the current DesignerStore.
 * @returns @ClientStore
 */
export default function getDesignerStore(): ClientStore | null {
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  if (window._designerStore !== undefined) {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    return window._designerStore
  } else {
    return null
  }
}
