import { toast, ToastContainer } from 'react-toastify'
import { createRoot } from 'react-dom/client'
import { createElement } from 'react'
import { getEmbrGlobals } from '@embr-js/perspective-client/src/globals'
import { merge } from 'lodash'

import './toast.css'

export function installToasts() {
  let toastRoot = document.getElementById('toast-root')
  if (!toastRoot) {
    toastRoot = document.createElement('div')
    toastRoot.id = 'toast-root'
    toastRoot.className = 'view-parent'

    const appContainer = document.getElementById('app-container')
    if (appContainer == null) {
      console.error('Failed to install toast extensions.')
      return
    }
    appContainer.append(toastRoot)
  }

  createRoot(toastRoot).render(createElement(ToastContainer))

  const embrGlobals = getEmbrGlobals()
  merge(embrGlobals.scripting.globals, {
    periscope: {
      toast,
    },
  })
}
