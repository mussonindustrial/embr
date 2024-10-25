import { ComponentRegistry } from '@inductiveautomation/perspective-client'
import {
    SwiperComponent,
    SwiperComponentMeta,
    AdvancedFlexRepeaterComponent,
    AdvancedFlexRepeaterComponentMeta
} from './components'

export { 
    AdvancedFlexRepeaterComponent,
    SwiperComponent,
}

const components = [
    new AdvancedFlexRepeaterComponentMeta(),
    new SwiperComponentMeta(),
]

components.forEach((c) => ComponentRegistry.register(c))
