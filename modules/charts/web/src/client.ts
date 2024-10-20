import {
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'

import {
    ChartjsComponent,
    ChartjsComponentMeta,
} from './components'

export { ChartjsComponent }

ComponentRegistry.register(ChartjsComponentMeta)
