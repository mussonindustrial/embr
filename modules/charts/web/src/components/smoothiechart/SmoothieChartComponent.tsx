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
import React, {
  MutableRefObject,
  RefObject,
  useCallback,
  useEffect,
  useMemo,
  useRef,
} from 'react'
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
  }
  redraw: boolean
}

type SmoothieChartRef = {
  appendData: (values: number[]) => void
  canvasRef: RefObject<HTMLCanvasElement>
  chartRef: MutableRefObject<SmoothieChart | null>
  props: ComponentProps<SmoothieChartProps>
  seriesRef: MutableRefObject<TimeSeries[]>
}

function setOptions(chart: SmoothieChart, nextOptions: IChartOptions) {
  const options = chart.options

  if (options && nextOptions) {
    Object.assign(options, nextOptions)
  }
}

function getOrDefault<T>(list: T[], index: number, fallback: T): T {
  if (index < 0 || index > list.length) {
    return fallback
  } else {
    return list[index]
  }
}

export function SmoothieChartComponent(
  props: ComponentProps<SmoothieChartProps>
) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const chartRef = useRef<SmoothieChart | null>(null)
  const seriesRef = useRef<TimeSeries[]>([])

  const transformedProps = useMemo(() => {
    console.log('transforming props')
    return transformProps(props.props, [
      getScriptTransform(props, props.store),
      getCSSTransform(canvasRef.current?.parentElement),
    ]) as SmoothieChartProps
  }, [props.props, canvasRef.current])

  const appendData = useCallback(
    (values: number[]) => {
      if (!seriesRef.current) {
        return
      }

      const timestamp = Date.now()
      for (let i = 0; i < seriesRef.current.length; i++) {
        const value = getOrDefault(values, i, i * 10)
        const series = seriesRef.current[i]
        series.append(timestamp, value)
      }
    },
    [seriesRef.current]
  )

  const renderChart = () => {
    if (!canvasRef.current) return

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
        canvasRef,
        chartRef,
        props,
        seriesRef,
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
      console.log('updating options')
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
      console.log('updating series')
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
  private chart?: SmoothieChartRef = undefined

  private proxyProps: JsObject = {}
  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

  private previousValues: number[] = []
  private timeoutId: NodeJS.Timeout | undefined

  constructor(component: AbstractUIElementStore) {
    super(component)
    this.updateStaleValues()
  }

  setInterface(chart: SmoothieChartRef) {
    this.chart = chart
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    if (this.jsProxy.handles(eventName)) {
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
    }

    if (eventName == 'data-update') {
      const values = eventObject['values'] as number[]
      this.notifyValues(values)
    }
  }

  getStaleDelay(): number {
    if (this.chart == undefined) {
      return 100
    } else {
      return this.chart.props.props.options.delayMillis
    }
  }

  updateStaleValues() {
    console.log('updating stale values')
    this.chart?.appendData(this.previousValues)
    this.timeoutId = setTimeout(() => {
      this.updateStaleValues()
    }, this.getStaleDelay())
  }

  notifyValues(values: number[]) {
    clearTimeout(this.timeoutId)
    this.chart?.appendData(values)
    this.previousValues = values
    this.timeoutId = setTimeout(() => {
      this.updateStaleValues()
    }, this.getStaleDelay())
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
      redraw: tree.read('redraw', undefined),
    } as never
  },
  getViewComponent: function (): PComponent {
    return SmoothieChartComponent as unknown as PComponent
  },
}
