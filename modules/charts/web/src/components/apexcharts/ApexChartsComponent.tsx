import React, { useCallback, useEffect, useRef } from 'react'
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
  ComponentDelegateJavaScriptProxy,
  ComponentEvents,
  ComponentLifecycleEvents,
  getScriptTransform,
  JavaScriptRunEvent,
  useComponentEvents,
  useDeepCompareMemo,
  useRefLifecycleEvents,
} from '@embr-js/perspective-client'
import { transformProps } from '@embr-js/utils'
import { ApexChartProps, Chart } from './react/ApexCharts'
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

export function ApexChartsComponent(props: ComponentProps<ChartProps>) {
  const chartRef = useRef<ApexCharts>()

  const transform = useCallback(
    (obj: unknown) =>
      transformProps(obj, [getScriptTransform(props, props.store)]),
    [chartRef.current]
  )

  const options = useDeepCompareMemo(() => {
    return transform(props.props.options) as ApexOptions
  }, [props.props.options])

  const series = useDeepCompareMemo(() => {
    return props.props.series
  }, [props.props.series])

  const events = useDeepCompareMemo(() => {
    return transform(props.props.events) as ChartEvents
  }, [props.props.events])

  useEffect(() => {
    const delegate = props.store.delegate as ApexChartsComponentDelegate
    delegate.setChart(chartRef.current ?? undefined)
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
  private jsProxy = new ComponentDelegateJavaScriptProxy(this)

  setChart(chart?: ApexCharts) {
    this.jsProxy.setRef(chart)
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
      options: tree.readObject('options', {}),
      series: tree.readArray('series', []),
      redraw: tree.readBoolean('redraw', false),
      events: tree.readObject('events', {}),
      style: tree.readStyle('style'),
    } as never
  },
  getViewComponent: function (): PComponent {
    return ApexChartsComponent as PComponent
  },
}
