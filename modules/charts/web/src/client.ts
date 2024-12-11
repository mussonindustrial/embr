import { ComponentRegistry } from '@inductiveautomation/perspective-client'

import { ChartJsComponent, ChartJsComponentMeta } from './components'

export { ChartJsComponent }

ComponentRegistry.register(ChartJsComponentMeta)
