import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
  SwiperComponent,
  SwiperComponentMeta,
  FlexRepeaterComponent,
  FlexRepeaterComponentMeta,
  EmbeddedViewComponent,
  EmbeddedViewComponentMeta,
} from './components'

export { FlexRepeaterComponent, SwiperComponent, EmbeddedViewComponent }

const components = [
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
  new EmbeddedViewComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))
