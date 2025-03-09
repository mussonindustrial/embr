import { ComponentRegistry } from '@inductiveautomation/perspective-client'

import {
  ApexChartsComponent,
  ApexChartsComponentMeta,
  ChartJsComponent,
  ChartJsComponentMeta,
} from './components'

export { ApexChartsComponent, ChartJsComponent }

ComponentRegistry.register(ApexChartsComponentMeta)
ComponentRegistry.register(ChartJsComponentMeta)
