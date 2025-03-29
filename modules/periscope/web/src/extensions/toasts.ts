import { toast, ToastContainer } from 'react-toastify'
import { createRoot } from 'react-dom/client'
import { createElement } from 'react'
import { addScriptingGlobals } from '@embr-js/perspective-client'

export function installToasts() {
  let toastRoot = document.getElementById('toast-root')
  if (!toastRoot) {
    toastRoot = document.createElement('div')
    toastRoot.id = 'toast-root'

    const appContainer = document.getElementById('app-container')
    if (appContainer == null) {
      console.error('Failed to install toast extensions.')
      return
    }
    appContainer.append(toastRoot)
  }

  createRoot(toastRoot).render(createElement(ToastContainer))

  addScriptingGlobals({ toast })
}
