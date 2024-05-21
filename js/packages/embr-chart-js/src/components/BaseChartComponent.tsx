import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart as Chartjs, ChartProps } from 'react-chartjs-2'
import { Chart } from 'chart.js'
import { recursiveMap } from '../util/iteration'
import {
    ChartScript,
    ContextScript,
    asCSSVar,
    asChartScript,
    asContextScript,
} from '../util/scriptableOptions'

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

const ContextScriptableProps = ['options', 'data', 'plugins'] as const
const ChartScriptableProps = ['events'] as const

export function BaseChartComponent(
    props: ComponentProps<PerspectiveChartProps>
) {
    const chartRef: React.MutableRefObject<PerspectiveChart | undefined> =
        React.useRef(undefined)

    const processedProps = props

    ContextScriptableProps.forEach((key) => {
        processedProps.props[key] = recursiveMap(
            processedProps.props[key],
            (value) => {
                let v = asContextScript(value, {
                    self: props,
                })
                v = asCSSVar(chartRef.current?.canvas.parentElement, v)
                return v as ContextScript
            }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ) as any
    })

    ChartScriptableProps.forEach((key) => {
        processedProps.props[key] = recursiveMap(
            processedProps.props[key],
            (value) => {
                return asChartScript(value, { self: props }) as ChartScript
            }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ) as any
    })

    callUserChartEvent(chartRef.current, processedProps.props, 'beforeRender')

    return (
        <div {...props.emit()}>
            <Chartjs
                type={processedProps.props.type}
                options={processedProps.props.options}
                data={processedProps.props.data}
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
