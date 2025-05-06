import React, { MutableRefObject, useCallback, useEffect, useRef } from 'react'
import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  JsObject,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentEvents,
  ComponentDelegateJavaScriptProxy,
  ComponentLifecycleEvents,
  getScriptTransform,
  JavaScriptRunEvent,
  useComponentEvents,
  useRefLifecycleEvents,
} from '@embr-js/perspective-client'
import { transformProps } from '@embr-js/utils'
import { ApexChartProps, Chart } from './ApexCharts'
import { useDeepCompareEffect, useFirstMountState } from 'react-use'
import { ApexOptions } from 'apexcharts'

export const COMPONENT_TYPE = 'embr.chart.apex-charts'

type ChartEvents = ComponentEvents & {
  chart?: {
    lifecycle?: ComponentLifecycleEvents
  }
}
type ChartProps = ApexChartProps & {
  events?: ChartEvents
  style?: StyleObject
}

function usePropertyMemo<T>(factory: () => T, deps: React.DependencyList) {
  const firstMount = useFirstMountState()
  const result = useRef<T>() as MutableRefObject<T>

  if (firstMount) {
    result.current = factory()
  }

  useDeepCompareEffect(() => {
    if (!firstMount) {
      result.current = factory()
    }
  }, deps)
  return result.current
}

export function ApexChartsComponent(props: ComponentProps<ChartProps>) {
  const chartRef = useRef<ApexCharts>(null)

  const transform = useCallback(
    (obj: unknown) =>
      transformProps(obj, [getScriptTransform(props, props.store)]),
    [chartRef.current]
  )

  const options = usePropertyMemo(() => {
    return transform(props.props.options) as ApexOptions
  }, [props.props.options])

  const series = usePropertyMemo(() => {
    return props.props.series
  }, [props.props.series])

  const events = usePropertyMemo(() => {
    return transform(props.props.events) as ChartEvents
  }, [props.props.events])

  // Store Chart Reference with Delegate
  useEffect(() => {
    const delegate = props.store.delegate as ApexChartsComponentDelegate
    delegate.setChart(chartRef.current)
  }, [props.store.delegate, chartRef.current])

  // Lifecycle Events
  useComponentEvents(props.store, events ?? {}, chartRef.current)
  useRefLifecycleEvents(events?.chart?.lifecycle ?? {}, chartRef.current)

  return (
    <div {...props.emit()}>
      <Chart
        ref={chartRef}
        type={props.props.type}
        options={options}
        series={series}
        redraw={props.props.redraw}
        width={'100%'}
        height={'100%'}
      />
    </div>
  )
}

class ApexChartsComponentDelegate extends ComponentStoreDelegate {
  private proxyProps: JsObject = {}

  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

  setChart(chart: ApexCharts | null) {
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
      redraw: tree.read('redraw', false),
      events: tree.read('events', {}),
      style: tree.readStyle('style'),
    } as never
  },
  getViewComponent: function (): PComponent {
    return ApexChartsComponent as PComponent
  },
}
