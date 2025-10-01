import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import { makeComponent, TestComponent } from './components'

import '@fontsource/inter'

import { Button, ButtonGroup } from '@mui/joy'
import { waitForClientStore } from '@embr-js/perspective-client'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'

const components = [
  makeComponent('embr.muijoy.test', TestComponent),
  makeComponent('embr.muijoy.input.button', Button),
  makeComponent('embr.muijoy.input.button-group', ButtonGroup),
]

components.forEach((c) => ComponentRegistry.register(c.meta))

waitForClientStore((clientStore) => {
  if (clientStore.isDesigner) {
    components.forEach((c) => {
      if (c.designDelegate) {
        InteractionRegistry.registerInteractionDelegates(c.designDelegate)
      }
    })
  }
})
