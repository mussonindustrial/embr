import { ChartDataset, ChartOptions, ChartType } from 'chart.js'

export type PChartOptions = Pick<
    ChartOptions,
    | 'animation'
    | 'animations'
    | 'aspectRatio'
    | 'backgroundColor'
    | 'borderColor'
    | 'clip'
    | 'color'
    | 'devicePixelRatio'
    | 'font'
    | 'hover'
    | 'indexAxis'
    | 'layout'
    | 'locale'
    | 'maintainAspectRatio'
    | 'normalized'
    | 'resizeDelay'
    | 'responsive'
    | 'scales'
    | 'transitions'
>

export type PChartDataset = Pick<ChartDataset, keyof ChartDataset>
export type PChartData = {
    labels?: string[]
    xLabels?: string[]
    yLabels?: string[]
    datasets: PChartDataset[]
}

export type ChartJSComponentProps = {
    type: ChartType
    options: PChartOptions
    data: PChartData
    plugins: Record<string, string>
}
