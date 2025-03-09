import React, { MutableRefObject, useEffect, useMemo, useRef } from 'react'
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
import { Chart as ChartJs, ChartProps } from 'react-chartjs-2'
import { Chart } from 'chart.js'
import { getCSSTransform, getScriptTransform } from '../../util'
import { transformProps } from '@embr-js/utils'
import { unset, cloneDeep } from 'lodash'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from '@embr-js/perspective-client'

export const COMPONENT_TYPE = 'embr.chart.chart-js'

type PerspectiveChart = Chart
type PerspectiveChartData = unknown[]
type ChartEvent = (chart: PerspectiveChart | undefined) => void
type PerspectiveChartEvents = {
  onMount?: ChartEvent
  onRender?: ChartEvent
  onUnmount?: ChartEvent
  /** @deprecated */
  beforeRender?: ChartEvent
}
type UpdateMode =
  | 'resize'
  | 'reset'
  | 'none'
  | 'hide'
  | 'show'
  | 'default'
  | 'active'
  | 'zoom'
  | undefined
type PerspectiveChartProps = ChartProps & {
  events?: PerspectiveChartEvents
  updateMode?: UpdateMode
  redraw?: boolean
}

function callUserChartEvent(
  chart: PerspectiveChart | undefined,
  props: PerspectiveChartProps,
  event: keyof PerspectiveChartEvents
) {
  if (chart === undefined || props.events === undefined) {
    return
  }

  const userFunction = props.events[event]
  if (userFunction !== undefined && typeof userFunction == 'function') {
    userFunction(chart)
  }
}

function extractPropsData(props: PerspectiveChartProps) {
  const localProps = cloneDeep(props)
  const data: PerspectiveChartData[] = []

  localProps.data?.datasets?.forEach((dataset) => {
    data.push(dataset.data ? dataset.data : [])
    unset(dataset, 'data')
  })
  return { props, data }
}

function installPropsData(
  props: PerspectiveChartProps,
  data: PerspectiveChartData[]
) {
  data.forEach((data, index) => {
    props.data.datasets[index].data = data
  })
}

export function ChartJsComponent(props: ComponentProps<PerspectiveChartProps>) {
  const chartRef: MutableRefObject<PerspectiveChart | undefined> =
    useRef(undefined)

  // Register the chart with the component delegate
  useEffect(() => {
    const delegate = props.store.delegate as ChartJsComponentDelegate
    delegate.setChart(chartRef.current)
  }, [props.store.delegate, chartRef.current])

  // Apply transforms to the user supplied properties
  const transformedProps = useMemo(() => {
    const { props: configProps, data } = extractPropsData(props.props)

    const transformedProps = transformProps(configProps, [
      getScriptTransform(props, props.store),
      getCSSTransform(chartRef.current?.canvas.parentElement),
    ]) as PerspectiveChartProps

    installPropsData(transformedProps, data)
    return transformedProps
  }, [props.props])

  // Call component lifecycle events
  callUserChartEvent(chartRef.current, transformedProps, 'onRender')
  callUserChartEvent(chartRef.current, transformedProps, 'beforeRender')
  useEffect(() => {
    callUserChartEvent(chartRef.current, transformedProps, 'onMount')

    return () => {
      callUserChartEvent(chartRef.current, transformedProps, 'onUnmount')
    }
  }, [])

  return (
    <div {...props.emit()}>
      <ChartJs
        ref={chartRef}
        type={transformedProps.type}
        options={transformedProps.options}
        data={transformedProps.data}
        plugins={transformedProps.plugins}
        redraw={transformedProps.redraw}
        updateMode={transformedProps.updateMode}
      />
    </div>
  )
}

class ChartJsComponentDelegate extends ComponentStoreDelegate {
  private proxyProps: JsObject = {}

  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

  setChart(chart?: PerspectiveChart) {
    this.proxyProps.chart = chart
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    if (this.jsProxy.handles(eventName)) {
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
    }
  }
}

export const ChartJsComponentMeta: ComponentMeta = {
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
    return new ChartJsComponentDelegate(component)
  },
  getPropsReducer(tree: PropertyTree): PerspectiveChartProps {
    return {
      type: tree.readString('type'),
      options: tree.read('options', {}),
      data: tree.read('data', {}),
      plugins: tree.readArray('plugins', []),
      redraw: tree.read('redraw', undefined),
      updateMode: tree.read('updateMode', undefined),
      events: tree.read('events', {}),
    } as never
  },
  getViewComponent: function (): PComponent {
    return ChartJsComponent as PComponent
  },
}
