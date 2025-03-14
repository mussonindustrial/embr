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
import { Chart, UpdateMode } from 'chart.js'
import { transformProps } from '@embr-js/utils'
import { unset, cloneDeep } from 'lodash'
import {
  ComponentDelegateJavaScriptProxy,
  ComponentEvents,
  getCSSTransform,
  getScriptTransform,
  JavaScriptRunEvent,
  useComponentEvents,
} from '@embr-js/perspective-client'

export const COMPONENT_TYPE = 'embr.chart.chart-js'

type ChartData = unknown[]
type ChartComponentProps = ChartProps & {
  events?: ComponentEvents & {
    /** @deprecated use `onUpdate` */
    beforeRender?: (obj: unknown) => void
  }
  updateMode?: UpdateMode
  redraw?: boolean
}

function extractPropsData(props: ChartComponentProps) {
  const localProps = cloneDeep(props)
  const data: ChartData[] = []

  localProps.data?.datasets?.forEach((dataset) => {
    data.push(dataset.data ? dataset.data : [])
    unset(dataset, 'data')
  })
  return { props, data }
}

function installPropsData(props: ChartComponentProps, data: ChartData[]) {
  data.forEach((data, index) => {
    props.data.datasets[index].data = data
  })
}

export function ChartJsComponent(props: ComponentProps<ChartComponentProps>) {
  const chartRef: MutableRefObject<Chart | undefined> = useRef(undefined)

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
    ]) as ChartComponentProps

    installPropsData(transformedProps, data)
    return transformedProps
  }, [props.props])

  // Call component lifecycle events
  useComponentEvents(
    props.store,
    transformedProps.events ?? {},
    chartRef.current
  )

  const beforeRender_DEPRECATED = transformedProps.events?.beforeRender
  if (beforeRender_DEPRECATED && typeof beforeRender_DEPRECATED == 'function') {
    beforeRender_DEPRECATED(chartRef.current)
  }

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

  setChart(chart?: Chart) {
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
  getPropsReducer(tree: PropertyTree): ChartComponentProps {
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
