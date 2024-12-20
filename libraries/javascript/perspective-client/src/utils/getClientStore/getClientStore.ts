import { ClientStore } from '@inductiveautomation/perspective-client'
import getDesignerStore from '../getDesignerStore'

/**
 * Get the current ClientStore.
 * @returns @ClientStore
 */
export default function getClientStore(): ClientStore | undefined {
  if (window.__client !== undefined) {
    const clientStore = window.__client
    if (clientStore.isDesigner) {
      return getDesignerStore()
    } else {
      return clientStore
    }
  } else if (getDesignerStore()) {
    return getDesignerStore()
  } else {
    return undefined
  }
}
