import { ClientStore } from '@inductiveautomation/perspective-client'
import { getClientStore } from '../index'

export default async function waitForClientStore(): Promise<ClientStore> {
  const existing = getClientStore()

  if (existing) {
    return existing
  }

  return new Promise((resolve) => {
    const interval = setInterval(() => {
      const store = getClientStore()

      if (store) {
        clearInterval(interval)
        resolve(store)
      }
    }, 0)
  })
}
