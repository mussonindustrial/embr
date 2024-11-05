import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
    SwiperComponent,
    SwiperComponentMeta,
    FlexRepeaterComponent,
    FlexRepeaterComponentMeta
} from './components'

export { 
    FlexRepeaterComponent,
    SwiperComponent,
}

const components = [
    new FlexRepeaterComponentMeta(),
    new SwiperComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))
