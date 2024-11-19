import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
    SwiperComponent,
    SwiperComponentMeta,
    FlexRepeaterComponent,
    FlexRepeaterComponentMeta,
    ComponentRepeaterComponent,
    ComponentRepeaterComponentMeta
} from './components'

export { 
    ComponentRepeaterComponent,
    FlexRepeaterComponent,
    SwiperComponent,
}

const components = [
    new ComponentRepeaterComponentMeta(),
    new FlexRepeaterComponentMeta(),
    new SwiperComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))
