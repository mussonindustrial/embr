import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'

export * from './runJavaScript'

export function installExtensions(clientStore: ClientStore) {
  installRunJavaScript(clientStore)
}
