import 'chartjs-adapter-moment'

import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

import AnnotationPlugin from 'chartjs-plugin-annotation'
Chart.register(AnnotationPlugin)

import ZoomPlugin from 'chartjs-plugin-zoom'
Chart.register(ZoomPlugin)

import {
    BoxPlotController,
    ViolinController,
    BoxAndWiskers,
    Violin,
    ViolinChart,
} from '@sgratzl/chartjs-chart-boxplot'
Chart.register(
    BoxPlotController,
    ViolinController,
    BoxAndWiskers,
    Violin,
    ViolinChart
)

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
    TreeChart,
    TreeController
} from 'chartjs-chart-graph'
Chart.register(
    ForceDirectedGraphController,
    EdgeLine,
    DendogramChart,
    DendogramController,
    DendrogramChart,
    DendrogramController,
    ForceDirectedGraphChart,
    GraphChart,
    GraphController,
    TreeChart,
    TreeController
)

import { MatrixController, MatrixElement } from 'chartjs-chart-matrix'
Chart.register(MatrixController, MatrixElement)

import {
    LinearAxis,
    LineSegment,
    ParallelCoordinatesController,
    PCPScale,
} from 'chartjs-chart-pcp'
Chart.register(LineSegment, ParallelCoordinatesController, PCPScale)
Chart.registry.addElements(LinearAxis)

import { SankeyController, Flow } from 'chartjs-chart-sankey'
Chart.register(SankeyController, Flow)

import ChartjsPluginStacked100 from 'chartjs-plugin-stacked100'
Chart.register(ChartjsPluginStacked100)

import { TreemapController, TreemapElement } from 'chartjs-chart-treemap'
Chart.register(TreemapController, TreemapElement)

import ChartDataLabels from 'chartjs-plugin-datalabels'
Chart.register(ChartDataLabels)

import { HierarchicalScale } from 'chartjs-plugin-hierarchical'
Chart.registry.addElements(HierarchicalScale)

import Autocolors from 'chartjs-plugin-autocolors'
Chart.register(Autocolors)

import { CrosshairPlugin } from 'chartjs-plugin-crosshair'
Chart.register(CrosshairPlugin)

import { FunnelController, TrapezoidElement } from 'chartjs-chart-funnel'
Chart.register(FunnelController, TrapezoidElement)

import {
    VennDiagramController,
    ArcSlice,
    EulerDiagramController,
} from 'chartjs-chart-venn'
Chart.register(VennDiagramController, ArcSlice, EulerDiagramController)

import { WordCloudController, WordElement } from 'chartjs-chart-wordcloud'
Chart.register(WordCloudController, WordElement)