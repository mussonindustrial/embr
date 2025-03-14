import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
  SwiperComponent,
  SwiperComponentMeta,
  FlexRepeaterComponent,
  FlexRepeaterComponentMeta,
  EmbeddedViewComponent,
  EmbeddedViewComponentMeta,
  JsonViewComponent,
  JsonViewComponentMeta,
  PortalComponent,
  PortalComponentMeta,
} from './components'
import { installExtensions } from './extensions'
import { waitForClientStore } from '@embr-js/perspective-client'

export {
  FlexRepeaterComponent,
  SwiperComponent,
  EmbeddedViewComponent,
  JsonViewComponent,
  PortalComponent,
}

const components = [
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
  new EmbeddedViewComponentMeta(),
  new JsonViewComponentMeta(),
  new PortalComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))

waitForClientStore((clientStore) => installExtensions(clientStore))
