import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart as Chartjs, ChartProps } from 'react-chartjs-2'
import { Chart, ChartTypeRegistry, ScriptableContext } from 'chart.js'
import { recursiveMap } from '../util/iteration'
import {
    parseChartFunctionString,
    parseContextFunctionString,
} from '../util/user-functions'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js'

export type OptionScript = (
    context: ScriptableContext<keyof ChartTypeRegistry>,
    options: object
) => unknown
export type ScriptableOption = OptionScript | string
const ContextScriptableProps = ['options', 'data', 'plugins'] as const
const ChartScriptableProps = ['events'] as const

function processRawProps(props: PerspectiveChartProps) {
    for (const key of ContextScriptableProps) {
        props[key] = recursiveMap(props[key], (value) => {
            switch (typeof value) {
                case 'string':
                    return parseContextFunctionString(value)
                default:
                    return value
            }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        }) as any
    }

    for (const key of ChartScriptableProps) {
        props[key] = recursiveMap(props[key], (value) => {
            switch (typeof value) {
                case 'string':
                    return parseChartFunctionString(value)
                default:
                    return value
            }
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        }) as any
    }
    return props
}

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
    const chartRef: React.MutableRefObject<PerspectiveChart | undefined> =
        React.useRef(undefined)

    props.props = processRawProps(props.props) as PerspectiveChartProps
    callUserChartEvent(chartRef.current, props.props, 'beforeRender')

    return (
        <div {...props.emit()}>
            <Chartjs
                type={props.props.type}
                options={props.props.options}
                data={props.props.data}
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
