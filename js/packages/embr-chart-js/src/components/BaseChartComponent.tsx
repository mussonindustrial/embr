import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart, ChartProps } from 'react-chartjs-2'
import { ChartTypeRegistry, ScriptableContext } from 'chart.js'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js'

export type OptionScript = (
    context: ScriptableContext<keyof ChartTypeRegistry>,
    options: object
) => unknown

function recursiveMap<T>(obj: T, callback: (obj: unknown) => unknown): unknown {
    if (Array.isArray(obj)) return obj.map((v) => recursiveMap(v, callback))
    if (obj && typeof obj === 'object')
        return Object.fromEntries(
            Object.entries(obj).map(([k, v]) => [k, recursiveMap(v, callback)])
        )
    return callback(obj)
}

function transformFunctionString(property: string): OptionScript | string {
    if (property.includes('return ')) {
        return new Function('context', 'options', property) as OptionScript
    }
    return property
}

function transformCSSVariableString(property: string): OptionScript | string {
    if (property.startsWith('var(--')) {
        const propertyName = property.match(/(?<=var\()[\w-]+/)
        if (propertyName === null) {
            return property
        }
        return new Function(
            'context',
            'options',
            `return window.getComputedStyle(context.chart.canvas.parentElement).getPropertyValue('${propertyName[0]}')`
        ) as OptionScript
    }
    return property
}

function transformProps(props: ChartProps) {
    return recursiveMap(props, (object) => {
        if (typeof object === 'string') {
            object = transformCSSVariableString(object)
            if (typeof object === 'string') {
                object = transformFunctionString(object)
            }
        }
        return object
    })
}

export function BaseChartComponent(props: ComponentProps<ChartProps>) {
    props.props = transformProps(props.props) as ChartProps
    return (
        <div {...props.emit()}>
            <Chart
                type={props.props.type}
                options={props.props.options}
                data={props.props.data}
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

    getPropsReducer(tree: PropertyTree): ChartProps {
        return {
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
        } as never
    }

    getViewComponent(): PComponent {
        return BaseChartComponent as PComponent
    }
}
