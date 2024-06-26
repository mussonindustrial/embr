import React, { MutableRefObject, useRef } from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart as Chartjs, ChartProps } from 'react-chartjs-2'
import { Chart } from 'chart.js'
import { getCSSTransform, getScriptTransform } from '../util/propTransforms'
import { transformProps } from '@mussonindustrial/embr-utils'
import { unset, cloneDeep } from 'lodash'

export const ChartjsComponentMeta: ComponentMeta = {
    getComponentType: function (): string {
        return 'embr.chart.chart-js'
    },
    getDefaultSize: function (): SizeObject {
        return {
            width: 300,
            height: 300,
        }
    },
    getViewComponent: function (): PComponent {
        return ChartjsComponent as PComponent
    },
    getPropsReducer(tree: PropertyTree): PerspectiveChartProps {
        return {
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
            events: tree.read('events', {}),
        } as never
    },
}

type PerspectiveChart = Chart
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type PerspectiveChartData = any[]
type ChartEvent = (chart: PerspectiveChart | undefined) => void
type PerspectiveChartEvents = {
    beforeRender?: ChartEvent
}
type PerspectiveChartProps = ChartProps & {
    events?: PerspectiveChartEvents
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

    localProps.data.datasets.forEach((dataset) => {
        data.push(dataset.data)
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

export function ChartjsComponent(props: ComponentProps<PerspectiveChartProps>) {
    const chartRef: MutableRefObject<PerspectiveChart | undefined> =
        useRef(undefined)

    const { props: configProps, data } = extractPropsData(props.props)
    const transformedProps = transformProps(configProps, [
        getScriptTransform({ self: props, client: window.__client }),
        getCSSTransform(chartRef.current?.canvas.parentElement),
    ]) as PerspectiveChartProps

    installPropsData(transformedProps, data)
    callUserChartEvent(chartRef.current, transformedProps, 'beforeRender')

    return (
        <div {...props.emit()}>
            <Chartjs
                type={transformedProps.type}
                options={transformedProps.options}
                data={transformedProps.data}
                plugins={transformedProps.plugins}
                ref={chartRef}
            />
        </div>
    )
}
