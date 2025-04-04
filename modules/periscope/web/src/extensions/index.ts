import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'
import { installToasts } from './toast'
import { installImportMap } from './import-map'

export * from './runJavaScript'

export function installExtensions(clientStore: ClientStore) {
  installImportMap()
  installRunJavaScript(clientStore)
  installToasts(clientStore)
}
