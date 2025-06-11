import { ComponentRegistry } from '@inductiveautomation/perspective-client'

import { ApexChartsComponent, ApexChartsComponentMeta } from './components'
export { ApexChartsComponent }
ComponentRegistry.register(ApexChartsComponentMeta)

import { ApexChartLegacyComponent, ApexChartLegacyMeta } from './components'
export { ApexChartLegacyComponent }
ComponentRegistry.register(new ApexChartLegacyMeta())

import { ChartJsComponent, ChartJsComponentMeta } from './components'
export { ChartJsComponent }
ComponentRegistry.register(ChartJsComponentMeta)
