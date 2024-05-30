import {
    ComponentMeta,
    ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import { Chart, registerables } from 'chart.js'
import 'chartjs-adapter-moment'
import AnnotationPlugin from 'chartjs-plugin-annotation'
import ZoomPlugin from 'chartjs-plugin-zoom'
import {
    BoxPlotController,
    ViolinController,
    BoxAndWiskers,
    Violin,
    ViolinChart,
} from '@sgratzl/chartjs-chart-boxplot'
import {
    ForceDirectedGraphController,
    EdgeLine,
    DendogramChart,
    DendogramController,
    DendrogramChart,
    DendrogramController,
    ForceDirectedGraphChart,
    GraphChart,
    GraphController,
} from 'chartjs-chart-graph'
import { MatrixController, MatrixElement } from 'chartjs-chart-matrix'
import {
    LinearAxis,
    LineSegment,
    ParallelCoordinatesController,
    PCPScale,
} from 'chartjs-chart-pcp'
import { SankeyController, Flow } from 'chartjs-chart-sankey'
import ChartjsPluginStacked100 from 'chartjs-plugin-stacked100'
import Gradient from 'chartjs-plugin-gradient'
import { TreemapController, TreemapElement } from 'chartjs-chart-treemap'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import { HierarchicalScale } from 'chartjs-plugin-hierarchical'
import Autocolors from 'chartjs-plugin-autocolors'
import {
    BaseChartComponent,
    BaseChartComponentMeta,
} from './components/BaseChartComponent'

Chart.register(
    ...registerables,
    AnnotationPlugin,
    Autocolors,
    BoxPlotController,
    BoxAndWiskers,
    ChartDataLabels,
    ChartjsPluginStacked100,
    DendogramChart,
    DendogramController,
    DendrogramChart,
    DendrogramController,
    EdgeLine,
    Flow,
    ForceDirectedGraphController,
    ForceDirectedGraphChart,
    Gradient,
    GraphChart,
    GraphController,
    HierarchicalScale,
    LinearAxis,
    LineSegment,
    MatrixController,
    MatrixElement,
    ParallelCoordinatesController,
    PCPScale,
    SankeyController,
    TreemapController,
    TreemapElement,
    Violin,
    ViolinChart,
    ViolinController,
    ZoomPlugin
)
Chart.registry.addElements(LinearAxis, HierarchicalScale)

export { BaseChartComponent }

const components: Array<ComponentMeta> = [new BaseChartComponentMeta()]
components.forEach((c: ComponentMeta) => ComponentRegistry.register(c))
