import React, { useEffect, useMemo, useRef } from 'react'
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
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import { Charts } from './local-react-apexcharts'
import { Props } from 'react-apexcharts'
import { getCSSTransform, getScriptTransform } from '../../util'
import { transformProps } from '@embr-js/utils'
import { unset, cloneDeep } from 'lodash'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from '@embr-js/perspective-client'

export const COMPONENT_TYPE = 'embr.chart.apex-charts'

type ChartSeries = ApexAxisChartSeries | ApexNonAxisChartSeries | undefined
type ChartEvent = (chart: Charts | undefined) => void
type ChartEvents = {
  beforeRender?: ChartEvent
}

type ChartProps = Props & {
  events?: ChartEvents
}

function callUserChartEvent(
  chart: Charts | null,
  props: ChartProps,
  event: keyof ChartEvents
) {
  if (chart === null || props.events === undefined) {
    return
  }

  const userFunction = props.events[event]
  if (userFunction !== undefined && typeof userFunction == 'function') {
    userFunction(chart)
  }
}

function extractSeries(props: ChartProps) {
  const localProps = cloneDeep(props)
  const series = localProps.series

  unset(localProps, 'series')

  return { props, series }
}

function installSeries(props: ChartProps, series: ChartSeries) {
  props.series = series
}

export function ApexChartsComponent(props: ComponentProps<ChartProps>) {
  const divRef = useRef<HTMLDivElement>(null)
  const chartRef = useRef<Charts>(null)

  useEffect(() => {
    const delegate = props.store.delegate as ApexChartsComponentDelegate
    delegate.setChart(chartRef.current)
  }, [props.store.delegate, chartRef.current])

  const transformedProps = useMemo(() => {
    const { props: configProps, series } = extractSeries(props.props)

    const transformedProps = transformProps(configProps, [
      getScriptTransform(props, props.store),
      getCSSTransform(divRef.current),
    ]) as ChartProps

    installSeries(transformedProps, series)
    return transformedProps
  }, [props.props])

  callUserChartEvent(chartRef.current, transformedProps, 'beforeRender')

  return (
    <div ref={divRef} {...props.emit()}>
      <Charts
        ref={chartRef}
        type={transformedProps.type}
        options={transformedProps.options}
        series={transformedProps.series}
        width={'100%'}
        height={'100%'}
      />
    </div>
  )
}

class ApexChartsComponentDelegate extends ComponentStoreDelegate {
  private proxyProps: JsObject = {}

  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

  setChart(chart: Charts | null) {
    this.proxyProps.chart = chart
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    if (this.jsProxy.handles(eventName)) {
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
    }
  }
}

export const ApexChartsComponentMeta: ComponentMeta = {
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
    return new ApexChartsComponentDelegate(component)
  },
  getPropsReducer(tree: PropertyTree): ChartProps {
    return {
      type: tree.readString('type'),
      options: tree.read('options', {}),
      series: tree.read('series', {}),
    } as never
  },
  getViewComponent: function (): PComponent {
    return ApexChartsComponent as PComponent
  },
}
