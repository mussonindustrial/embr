import { ClientStore } from '@inductiveautomation/perspective-client'
import { installRunJavaScript } from './runJavaScript'
import { installToasts } from './toasts'

export * from './runJavaScript'

export function installExtensions(clientStore: ClientStore) {
  installRunJavaScript(clientStore)
  installToasts()
}
