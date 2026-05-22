import { ClientResourceStore } from '@/stores'

let storePromise: Promise<ClientResourceStore> | undefined
export default async function waitForClientResourceStore(): Promise<ClientResourceStore> {
  if (!storePromise) {
    storePromise = new Promise((resolve) => {
      const interval = setInterval(() => {
        const store = Embr?.clientResources
        if (store) {
          clearInterval(interval)
          resolve(store)
        }
      }, 0)
    })
  }

  return await storePromise
}
