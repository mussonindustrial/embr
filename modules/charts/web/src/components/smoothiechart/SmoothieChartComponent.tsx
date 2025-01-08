import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  JsObject,
  PComponent,
  PropertyTree,
  SizeObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from '@embr-js/perspective-client'
import React, { useCallback, useEffect, useMemo, useRef } from 'react'
import {
  IChartOptions,
  ITimeSeriesOptions,
  ITimeSeriesPresentationOptions,
  SmoothieChart,
  TimeSeries,
} from 'smoothie'
import { transformProps } from '@embr-js/utils'
import { getCSSTransform, getScriptTransform } from '../../util'

const COMPONENT_TYPE = 'embr.chart.smoothie-chart'

type SeriesProps = ITimeSeriesOptions & ITimeSeriesPresentationOptions

type SmoothieChartProps = {
  series: SeriesProps[]
  options: IChartOptions & {
    delayMillis: number
    update: {
      interval: number
    }
  }
  redraw: boolean
}

type ChartDataPoint = number | { value: number; timestamp: number }
type ChartSeriesData = ChartDataPoint | ChartDataPoint[]

type SmoothieChartRef = {
  appendData: (values: ChartSeriesData[]) => void
  canvas: HTMLCanvasElement | null
  chart: SmoothieChart | null
  props: ComponentProps<SmoothieChartProps>
  series: TimeSeries[]
}

function setOptions(chart: SmoothieChart, nextOptions: IChartOptions) {
  const options = chart.options

  if (options && nextOptions) {
    Object.assign(options, nextOptions)
  }
}

function getOrDefault<T>(list: T[], index: number, fallback: T): T {
  return index >= 0 && index < list.length ? list[index] : fallback
}

function appendToSeries(
  series: TimeSeries,
  data: ChartSeriesData,
  now = Date.now()
) {
  if (Array.isArray(data)) {
    data.forEach((point) => appendPoint(series, point, now))
  } else {
    appendPoint(series, data, now)
  }
}

function appendPoint(
  series: TimeSeries,
  point: ChartDataPoint,
  now = Date.now()
) {
  if (typeof point === 'number') {
    series.append(now, point)
  } else if (Array.isArray(point)) {
    series.append(getOrDefault(point, 0, now), getOrDefault(point, 1, 0))
  } else {
    series.append(point.timestamp, point.value)
  }
}

export function SmoothieChartComponent(
  props: ComponentProps<SmoothieChartProps>
) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<SmoothieChart | null>(null)
  const seriesRef = useRef<TimeSeries[]>([])

  const transformedProps = useMemo(() => {
    return transformProps(props.props, [
      getScriptTransform(props, props.store),
      getCSSTransform(canvasRef.current?.parentElement),
    ]) as SmoothieChartProps
  }, [props.props, canvasRef.current])

  const appendData = useCallback(
    (values: ChartSeriesData[]) => {
      if (!seriesRef.current) {
        return
      }

      const now = Date.now()
      for (let i = 0; i < seriesRef.current.length; i++) {
        const data = getOrDefault(values, i, [])
        const series = seriesRef.current[i]
        appendToSeries(series, data, now)
      }
    },
    [seriesRef.current]
  )

  const renderChart = () => {
    if (!canvasRef.current) return

    transformedProps.options.responsive = true
    chartRef.current = new SmoothieChart(transformedProps.options)
    seriesRef.current = []

    for (let i = 0; i < transformedProps.series.length; i++) {
      const seriesConfig = transformedProps.series[i]
      const series = new TimeSeries(seriesConfig)
      chartRef.current.addTimeSeries(series, seriesConfig)
      seriesRef.current[i] = series
    }

    chartRef.current.streamTo(
      canvasRef.current,
      transformedProps.options.delayMillis ?? 0
    )
    props.store.delegate?.fireEvent('renderChart', {})
  }

  const destroyChart = () => {
    if (chartRef.current) {
      chartRef.current.stop()
      chartRef.current = null
      seriesRef.current = []
    }
  }

  /* Update Delegate Interface */
  useEffect(() => {
    if (props.store.delegate) {
      const delegate = props.store.delegate as SmoothieChartComponentDelegate
      delegate.setInterface({
        appendData,
        canvas: canvasRef.current,
        chart: chartRef.current,
        props,
        series: seriesRef.current,
      })
    }
  }, [appendData, canvasRef, chartRef, seriesRef])

  /* Update Options */
  useEffect(() => {
    if (
      !transformedProps.redraw &&
      chartRef.current &&
      transformedProps.options
    ) {
      setOptions(chartRef.current, transformedProps.options)
    }
  }, [transformedProps.redraw, transformedProps.options])

  /* Update Series */
  useEffect(() => {
    if (
      !transformedProps.redraw &&
      seriesRef.current &&
      chartRef.current &&
      transformedProps.series
    ) {
      for (let i = 0; i < seriesRef.current.length; i++) {
        const series = seriesRef.current[i]
        const existingSeriesOptions =
          chartRef.current.getTimeSeriesOptions(series)

        const nextSeriesOptions = transformedProps.series[i]
        Object.assign(existingSeriesOptions, nextSeriesOptions)
      }
    }
  }, [transformedProps.redraw, transformedProps.series])

  /* Redraw */
  useEffect(() => {
    if (!chartRef.current) return

    if (transformedProps.redraw) {
      destroyChart()
      setTimeout(renderChart)
    }
  }, [
    transformedProps.redraw,
    transformedProps.options,
    transformedProps.series,
  ])

  /* Delay */
  useEffect(() => {
    destroyChart()
    setTimeout(renderChart)
  }, [props.props.options.delayMillis])

  /* Render */
  useEffect(() => {
    renderChart()

    return () => destroyChart()
  }, [])

  return (
    <div {...props.emit()}>
      <canvas ref={canvasRef} style={{ height: '100%', width: '100%' }} />
    </div>
  )
}

class SmoothieChartComponentDelegate extends ComponentStoreDelegate {
  private proxyProps: JsObject = {}
  private chart: SmoothieChartRef | null = null
  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

  setInterface(chart: SmoothieChartRef) {
    this.chart = chart
    this.proxyProps.chart = chart
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    if (this.jsProxy.handles(eventName)) {
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
    }

    if (eventName == 'data-append' && this.chart != null) {
      this.chart.appendData(eventObject['values'] as ChartSeriesData[])
    }
  }
}

export const SmoothieChartComponentMeta: ComponentMeta = {
  getComponentType: function (): string {
    return COMPONENT_TYPE
  },
  getDefaultSize: function (): SizeObject {
    return {
      width: 300,
      height: 300,
    }
  },
  createDelegate: function (
    component: AbstractUIElementStore
  ): ComponentStoreDelegate {
    return new SmoothieChartComponentDelegate(component)
  },
  getPropsReducer(tree: PropertyTree): SmoothieChartProps {
    return {
      series: tree.read('series', []),
      options: tree.read('options', {}),
      redraw: tree.read('redraw', false),
    } as never
  },
  getViewComponent: function (): PComponent {
    return SmoothieChartComponent as unknown as PComponent
  },
}
