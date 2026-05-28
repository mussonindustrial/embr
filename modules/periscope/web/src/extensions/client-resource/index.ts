import { ClientStore } from '@inductiveautomation/perspective-client'
import { installViewClientResourceSupport } from '@/extensions'
import { registerStore } from '@embr-js/perspective-client'
import { ClientResourceStore } from '@/stores'

export * from './ClientResourceComponent'
export * from './viewClientResourceSupport'

export async function installClientResources(clientStore: ClientStore) {
  const clientResourceStore = registerStore(
    'clientResources',
    () => new ClientResourceStore()
  )
  await clientResourceStore.initialize(clientStore)
  return clientResourceStore
}
installViewClientResourceSupport()
