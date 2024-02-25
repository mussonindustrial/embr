import {
    ComponentMeta,
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import { Chart, registerables } from 'chart.js'
import ChartStreaming from '@robloche/chartjs-plugin-streaming'
import zoomPlugin from 'chartjs-plugin-zoom'
import {
    TagHistoryChartComponent,
    TagHistoryChartComponentMeta,
} from './components/TagHistoryChart'
import {
    BaseChartComponent,
    BaseChartComponentMeta,
} from './components/BaseChartComponent'

Chart.register(...registerables, zoomPlugin, ChartStreaming)

export { BaseChartComponent, TagHistoryChartComponent }

const components: Array<ComponentMeta> = [
    new BaseChartComponentMeta(),
    new TagHistoryChartComponentMeta(),
]
components.forEach((c: ComponentMeta) => ComponentRegistry.register(c))
