import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'
import { installToasts } from './toast'
import { installClientResources } from './client-resource'

export * from './client-resource'
export * from './JoinableView'
export * from './toast'
export * from './runJavaScript'

export async function installExtensions(clientStore: ClientStore) {
  installRunJavaScript(clientStore)
  installToasts(clientStore)
  await installClientResources(clientStore)
}
