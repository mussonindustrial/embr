import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
  SwiperComponent,
  SwiperComponentMeta,
  FlexRepeaterComponent,
  FlexRepeaterComponentMeta,
  EmbeddedViewComponent,
  EmbeddedViewComponentMeta,
} from './components'
import { installExtensions } from './extensions'
import { waitForClientStore } from '@embr-js/perspective-client'

export { FlexRepeaterComponent, SwiperComponent, EmbeddedViewComponent }

const components = [
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
  new EmbeddedViewComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))

waitForClientStore((clientStore) => installExtensions(clientStore))
