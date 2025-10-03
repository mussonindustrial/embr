import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'
import { waitForClientStore } from '@embr-js/perspective-client'

import { makeComponent, Slot } from './components'
export * from './utilities'

import {
  Button,
  ButtonGroup,
  Dropdown,
  Menu,
  MenuButton,
  MenuItem,
} from '@mui/joy'
import '@fontsource/inter'

const components = [
  makeComponent('embr.muijoy.input.button', Button),
  makeComponent('embr.muijoy.input.button-group', ButtonGroup),
  makeComponent('embr.muijoy.input.dropdown', Dropdown),
  makeComponent('embr.muijoy.input.menu', Menu),
  makeComponent('embr.muijoy.input.menu-button', MenuButton),
  makeComponent('embr.muijoy.input.menu-item', MenuItem),
  makeComponent('embr.muijoy.slot', Slot),
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
