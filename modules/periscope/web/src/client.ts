import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
  CoordinateCanvasComponent,
  CoordinateCanvasComponentMeta,
  EmbeddedViewComponent,
  EmbeddedViewComponentMeta,
  FlexRepeaterComponent,
  FlexRepeaterComponentMeta,
  SwiperComponent,
  SwiperComponentMeta,
} from './components'

export {
  CoordinateCanvasComponent,
  FlexRepeaterComponent,
  SwiperComponent,
  EmbeddedViewComponent,
}

const components = [
  new CoordinateCanvasComponentMeta(),
  new EmbeddedViewComponentMeta(),
  new FlexRepeaterComponentMeta(),
  new SwiperComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))
