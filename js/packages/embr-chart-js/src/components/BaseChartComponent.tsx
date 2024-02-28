import * as React from 'react'
import { useRef } from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart, ChartProps } from 'react-chartjs-2'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js'

export function BaseChartComponent(props: ComponentProps<ChartProps>) {
    const chartRef = useRef(null)

    return (
        <div {...props.emit()}>
            <Chart
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
