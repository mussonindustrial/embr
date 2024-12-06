import { ClientStore } from '@inductiveautomation/perspective-client'
import { getClientStore } from '../index'

/**
 * Run a callback after the ClientStore is started.
 */
export default function waitForClientStore(
  callback: (clientStore: ClientStore) => void
) {
  setTimeout(function () {
    const clientStore = getClientStore()
    if (clientStore) {
      callback(clientStore)
    } else {
      waitForClientStore(callback)
    }
  }, 100)
}
