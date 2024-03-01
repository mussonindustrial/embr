import * as React from 'react'
import { useState } from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import _ from 'lodash'
import 'chartjs-adapter-luxon'
import { Chart, ChartProps } from 'react-chartjs-2'
import { zipLong } from '../util/arrays'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js.realtime'

export type RealtimeValue = {
    value: number
}

export type DataPoint = {
    x: number
    y: number
}

export type RealtimeData = DataPoint[]

export type RealtimeChartProps = ChartProps & {
    values: RealtimeValue[]
}

export function RealtimeChartComponent(
    props: ComponentProps<RealtimeChartProps>
) {
    const [realtimeData, setRealtimeData] = useState<RealtimeData[]>([])

    const onRefresh = () => {
        const zipped = zipLong(props.props.values, realtimeData)
        const now = Date.now()
        const newRealtimeData: RealtimeData[] = []

        zipped.forEach(
            ([newValue, existingValues]: [RealtimeValue, RealtimeData]) => {
                if (!existingValues) {
                    existingValues = []
                }
                existingValues.push({
                    x: now,
                    y: newValue.value,
                })
                newRealtimeData.push(existingValues)
            }
        )

        setRealtimeData(newRealtimeData)
    }

    const data = props.props.data
    for (let i = 0; i < realtimeData.length; i++) {
        if (data.datasets[i]) {
            data.datasets[i].data = realtimeData[i]
        }
    }

    const options = _.merge(props.props.options, {
        scales: {
            x: {
                type: 'realtime',
                realtime: {
                    onRefresh,
                },
            },
        },
    })

    return (
        <div {...props.emit()}>
            <Chart type={props.props.type} options={options} data={data} />
        </div>
    )
}

export class RealtimeChartComponentMeta implements ComponentMeta {
    getComponentType(): string {
        return COMPONENT_TYPE
    }

    getDefaultSize(): SizeObject {
        return {
            width: 300,
            height: 300,
        }
    }

    getPropsReducer(tree: PropertyTree): RealtimeChartProps {
        return {
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
            values: tree.readArray('values', []),
        } as never
    }

    getViewComponent(): PComponent {
        return RealtimeChartComponent as PComponent
    }
}
