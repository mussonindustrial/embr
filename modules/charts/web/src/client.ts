import { ComponentRegistry } from '@inductiveautomation/perspective-client'

import { ApexChartLegacyComponent, ApexChartLegacyMeta } from './components'
import { ChartJsComponent, ChartJsComponentMeta } from './components'

export { ApexChartLegacyComponent }
ComponentRegistry.register(new ApexChartLegacyMeta())

export { ChartJsComponent }
ComponentRegistry.register(ChartJsComponentMeta)
