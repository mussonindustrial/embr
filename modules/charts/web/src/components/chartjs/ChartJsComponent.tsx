import React, { MutableRefObject, useEffect, useMemo, useRef } from 'react'
import {
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
import { getClientStore, transformProps } from '@embr-js/utils'
import _, { unset, cloneDeep } from 'lodash'
import { AbstractUIElementStore } from '@inductiveautomation/perspective-client/build/dist/typedefs/stores/AbstractUIElementStore'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from './ComponentDelegateJavaScriptProxy'

export const COMPONENT_TYPE = 'embr.chart.chart-js'

type PerspectiveChart = Chart
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type PerspectiveChartData = any[]
type ChartEvent = (chart: PerspectiveChart | undefined) => void
type PerspectiveChartEvents = {
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

  useEffect(() => {
    if (props.store.delegate !== undefined) {
      ;(props.store.delegate as ChartJsComponentDelegate).setChart(
        chartRef.current
      )
    }
  }, [props.delegate, chartRef.current])

  const transformedProps = useMemo(() => {
    const { props: configProps, data } = extractPropsData(props.props)

    const transformedProps = transformProps(configProps, [
      getScriptTransform({ self: props, client: window.__client }),
      getCSSTransform(chartRef.current?.canvas.parentElement),
    ]) as PerspectiveChartProps

    installPropsData(transformedProps, data)
    return transformedProps
  }, [props.props])

  callUserChartEvent(chartRef.current, transformedProps, 'beforeRender')

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
  private chart: PerspectiveChart | undefined
  private jsProxy = new ComponentDelegateJavaScriptProxy(this, {})

  setChart(chart: PerspectiveChart | undefined) {
    this.chart = chart

    this.jsProxy.setGlobals({
      context: {
        client: getClientStore(),
        chart: this.chart,
        component: this.component,
      },
    })
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    console.log(eventName, eventObject)
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
