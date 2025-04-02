import { toast, ToastContainer } from 'react-toastify'
import { createRoot } from 'react-dom/client'
import { getEmbrGlobals } from '@embr-js/perspective-client/src/globals'
import { merge } from 'lodash'

import './toast.css'
import React from 'react'
import { ClientStore } from '@inductiveautomation/perspective-client'
import { observer } from 'mobx-react-lite'
import { DockOffset } from '@inductiveautomation/perspective-client/build/dist/typedefs/stores/MountStore'

type CenterToastContainerProps = {
  dockOffset: DockOffset
}

const CenterToastContainer = observer(
  ({ dockOffset }: CenterToastContainerProps) => {
    return (
      <ToastContainer
        style={{
          paddingTop: dockOffset.top.pixels,
          paddingLeft: dockOffset.left.pixels,
          paddingBottom: dockOffset.bottom.pixels,
          paddingRight: dockOffset.right.pixels,
          position: 'fixed',
          pointerEvents: 'none',
        }}
        toastStyle={{
          pointerEvents: 'all',
        }}
      />
    )
  }
)

export function installToasts(clientStore: ClientStore) {
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

  createRoot(toastRoot).render(
    <CenterToastContainer dockOffset={clientStore.mounts.dockOffset} />
  )

  const embrGlobals = getEmbrGlobals()
  merge(embrGlobals.scripting.globals, {
    periscope: {
      toast,
    },
  })
}
