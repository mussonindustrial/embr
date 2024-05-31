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

export const COMPONENT_TYPE = 'embr.chart.chart-js'

type PerspectiveChart = Chart

type ChartEvent = (chart: PerspectiveChart | undefined) => void

type PerspectiveChartProps = ChartProps & {
    events?: PerspectiveChartEvents
}

type PerspectiveChartEvents = {
    beforeRender?: ChartEvent
}

function callUserChartEvent(
    chart: PerspectiveChart | undefined,
    props: PerspectiveChartProps,
    event: keyof PerspectiveChartEvents
) {
    if (chart !== undefined) {
        if (props.events !== undefined) {
            const userFunction = props.events[event]
            if (
                userFunction !== undefined &&
                typeof userFunction == 'function'
            ) {
                userFunction(chart)
            }
        }
    }
}

export function BaseChartComponent(
    props: ComponentProps<PerspectiveChartProps>
) {
    const chartRef: MutableRefObject<PerspectiveChart | undefined> =
        useRef(undefined)

    const localProps = cloneDeep(props.props)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data: any[] = []

    localProps.data.datasets.forEach((dataset) => {
        data.push(dataset.data)
        unset(dataset, 'data')
    })

    const transformedProps = transformProps(localProps, [
        getScriptTransform({ self: props, client: window.__client }),
        getCSSTransform(chartRef.current?.canvas.parentElement),
    ]) as PerspectiveChartProps

    data.forEach((data, index) => {
        transformedProps.data.datasets[index].data = data
    })

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

export class BaseChartComponentMeta implements ComponentMeta {
    getComponentType(): string {
        return COMPONENT_TYPE
    }

    getDefaultSize(): SizeObject {
        return {
            width: 300,
            height: 300,
        }
    }

    getPropsReducer(tree: PropertyTree): PerspectiveChartProps {
        return {
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
            events: tree.read('events', {}),
        } as never
    }

    getViewComponent(): PComponent {
        return BaseChartComponent as PComponent
    }
}
