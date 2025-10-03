import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'
import { waitForClientStore } from '@embr-js/perspective-client'

import { makeComponent, Slot } from './components'
export * from './utilities'

import {
  Button,
  ButtonGroup,
  Checkbox,
  Dropdown,
  FormControl,
  FormHelperText,
  FormLabel,
  Input,
  Menu,
  MenuButton,
  MenuItem,
  Radio,
  RadioGroup,
  Slider,
} from '@mui/joy'

import '@fontsource/inter'
import './client.css'

const components = [
  makeComponent('embr.muijoy.input.button', Button),
  makeComponent('embr.muijoy.input.button-group', ButtonGroup),
  makeComponent('embr.muijoy.input.checkbox', Checkbox, (props) => ({
    onChange: (event) => {
      props.store.props.write('checked', event.target.checked)
    },
  })),
  makeComponent('embr.muijoy.input.form-control', FormControl),
  makeComponent('embr.muijoy.input.form-helper-text', FormHelperText),
  makeComponent('embr.muijoy.input.form-label', FormLabel),
  makeComponent('embr.muijoy.input.input', Input, (props) => ({
    onChange: (event) => {
      props.store.props.write('value', event.target.value)
    },
  })),
  makeComponent('embr.muijoy.input.radio', Radio),
  makeComponent('embr.muijoy.input.radio-group', RadioGroup, (props) => ({
    onChange: (event) => {
      props.store.props.write('value', event.target.value)
    },
  })),

  makeComponent('embr.muijoy.input.slider', Slider, (props) => ({
    onChange: (_, value) => {
      props.store.props.write('value', value)
    },
  })),
  makeComponent('embr.muijoy.nav.dropdown', Dropdown, (props) => ({
    onOpenChange: (_, isOpen) => {
      props.store.props.write('open', isOpen)
    },
  })),
  makeComponent('embr.muijoy.nav.menu', Menu),
  makeComponent('embr.muijoy.nav.menu-button', MenuButton),
  makeComponent('embr.muijoy.nav.menu-item', MenuItem),
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
