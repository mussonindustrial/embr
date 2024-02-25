import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart, ChartProps } from 'react-chartjs-2'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js'
export type ChartJSComponentProps = ChartProps

export function BaseChartComponent(
    props: ComponentProps<ChartJSComponentProps>
) {
    return (
        <div {...props.emit()}>
            <Chart
                type={props.store.props.read('type')}
                options={props.store.props.read('options')}
                data={props.store.props.read('data')}
                {...props.store.props.read('')}
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

    getPropsReducer(tree: PropertyTree): ChartJSComponentProps {
        return {
            type: tree.readString('type') as never,
            options: tree.read('options'),
            data: tree.readArray('data') as never,
        }
    }

    getViewComponent(): PComponent {
        return BaseChartComponent as PComponent
    }
}
