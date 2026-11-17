import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'
import { installToasts } from './toast'
import { installClientResources } from './client-resource'
import { installComponentDrop } from './componentDrop'
import { installComponentDefaults } from './componentDefaults'

export * from './componentEvents'
export * from './client-resource'
export * from './JoinableView'
export * from './toast'
export * from './runJavaScript'

export async function installExtensions(clientStore: ClientStore) {
  installComponentDefaults(clientStore)
  installRunJavaScript(clientStore)
  installToasts(clientStore)
  installComponentDrop()
  await installClientResources(clientStore)
}
