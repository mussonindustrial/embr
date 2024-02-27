import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart } from 'react-chartjs-2'
import { ChartJSComponentProps } from './BaseChartComponentProps'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js'

export function BaseChartComponent(
    props: ComponentProps<ChartJSComponentProps>
) {
    return (
        <div {...props.emit()}>
            <Chart
                type={props.store.props.read('type')}
                options={props.store.props.read('options')}
                data={props.store.props.read('data')}
                plugins={props.store.props.read('plugins')}
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
            type: tree.readString('type'),
            options: tree.read('options'),
            data: tree.readArray('data'),
            plugins: tree.readArray('plugins'),
        } as never
    }

    getViewComponent(): PComponent {
        return BaseChartComponent as PComponent
    }
}
