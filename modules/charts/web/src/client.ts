import { ComponentRegistry } from '@inductiveautomation/perspective-client'

import { ChartJsComponent, ChartJsComponentMeta } from './components'
import {
  SmoothieChartComponent,
  SmoothieChartComponentMeta,
} from './components'

export { ChartJsComponent, SmoothieChartComponent }

ComponentRegistry.register(ChartJsComponentMeta)
ComponentRegistry.register(SmoothieChartComponentMeta)
