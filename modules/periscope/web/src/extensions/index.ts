import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'
import { installToasts } from './toast'
import { installClientResources } from './client-resource'
import { installComponentDefaults } from './componentDefaults'
import { installComponentDrop } from './componentDrop'
import { installProjectComponentDesign } from './projectComponents'

export * from './componentDefaults'
export * from './componentDrop'
export * from './componentEvents'
export * from './client-resource'
export * from './JoinableView'
export * from './projectComponents'
export * from './toast'
export * from './runJavaScript'

export async function installExtensions(clientStore: ClientStore) {
  installComponentDefaults(clientStore)
  installRunJavaScript(clientStore)
  installToasts(clientStore)
  await installClientResources(clientStore)

  if (clientStore.isDesigner) {
    installComponentDrop(clientStore)
    await installProjectComponentDesign()
  }
}
