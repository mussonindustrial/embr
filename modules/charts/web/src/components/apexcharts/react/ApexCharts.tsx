import React, {
  useEffect,
  useRef,
  forwardRef,
  ReactNode,
  useMemo,
  useCallback,
} from 'react'
import ApexCharts from 'apexcharts'

import type { ApexOptions } from 'apexcharts'
import type { ForwardedRef } from 'react'
import { extend } from 'lodash'

export type ApexChartProps = {
  type: ApexChart['type']
  options: ApexOptions
  series: ApexOptions['series']
  width: number | string
  height: number | string
  redraw?: boolean
  fallbackContent?: ReactNode
}

export function reforwardRef<T>(ref: ForwardedRef<T>, value: T) {
  if (typeof ref === 'function') {
    ref(value)
  } else if (ref) {
    ref.current = value
  }
}

function ApexChartsComponent(
  props: ApexChartProps,
  ref: ForwardedRef<ApexCharts>
) {
  const {
    type = 'line',
    height = 350,
    width = '100%',
    options,
    series,
    redraw = false,
    fallbackContent,
    ...divProps
  } = props

  const chartRef = useRef<ApexCharts>()
  const containerRef = useRef<HTMLDivElement>(null)

  const chartOptions = useMemo(() => {
    return {
      ...options,
      chart: {
        type,
        height,
        width,
      },
    }
  }, [type, height, width, options])

  const renderChart = useCallback(() => {
    if (!containerRef.current) return

    const config = extend(chartOptions, { series })

    chartRef.current = new ApexCharts(containerRef.current, config)
    void chartRef.current.render()

    reforwardRef(ref, chartRef.current)
  }, [containerRef.current, chartOptions, series])

  const destroyChart = useCallback(() => {
    reforwardRef(ref, null)
    if (chartRef.current) {
      chartRef.current.destroy()
      chartRef.current = undefined
    }
  }, [chartRef.current])

  useEffect(() => {
    if (!redraw && chartRef.current && chartOptions) {
      void chartRef.current.updateOptions(chartOptions)
    }
  }, [redraw, chartOptions])

  useEffect(() => {
    if (!redraw && chartRef.current && series) {
      void chartRef.current.updateSeries(series)
    }
  }, [redraw, series])

  useEffect(() => {
    if (!chartRef.current) return

    if (redraw) {
      destroyChart()
      setTimeout(renderChart)
    }
  }, [redraw, chartOptions, series])

  useEffect(() => {
    if (!chartRef.current) return

    destroyChart()
    setTimeout(renderChart)
  }, [type])

  useEffect(() => {
    renderChart()
    return () => destroyChart()
  }, [])

  return (
    <div ref={containerRef} {...divProps}>
      {fallbackContent}
    </div>
  )
}

export const Chart = forwardRef(ApexChartsComponent)
