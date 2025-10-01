import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
  PortalComponent,
  PortalComponentMeta,
} from './components'

export {
  PortalComponent,
}

const components = [
  new PortalComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))