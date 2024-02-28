import {
    ComponentMeta,
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import { Chart, registerables } from 'chart.js'
import 'chartjs-adapter-luxon'
import AnnotationPlugin from 'chartjs-plugin-annotation'
import ZoomPlugin from 'chartjs-plugin-zoom'
import ChartStreaming from '@robloche/chartjs-plugin-streaming'

import {
    BaseChartComponent,
    BaseChartComponentMeta,
} from './components/BaseChartComponent'
import {
    RealtimeChartComponent,
    RealtimeChartComponentMeta,
} from './components/RealtimeChartComponent'

Chart.register(...registerables, AnnotationPlugin, ZoomPlugin, ChartStreaming)

export { BaseChartComponent, RealtimeChartComponent }

const components: Array<ComponentMeta> = [
    new BaseChartComponentMeta(),
    new RealtimeChartComponentMeta(),
]
components.forEach((c: ComponentMeta) => ComponentRegistry.register(c))
