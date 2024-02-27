import {
    ComponentMeta,
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import { Chart, registerables } from 'chart.js'
import 'chartjs-adapter-luxon'
import ChartStreaming from '@robloche/chartjs-plugin-streaming'
import zoomPlugin from 'chartjs-plugin-zoom'
import {
    BaseChartComponent,
    BaseChartComponentMeta,
} from './components/BaseChartComponent'

Chart.register(...registerables, zoomPlugin, ChartStreaming)

export { BaseChartComponent }

const components: Array<ComponentMeta> = [new BaseChartComponentMeta()]
components.forEach((c: ComponentMeta) => ComponentRegistry.register(c))