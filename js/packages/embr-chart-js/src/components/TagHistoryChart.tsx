import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { Chart as ChartJS } from 'chart.js'
import { Chart, ChartProps } from 'react-chartjs-2'
import { useEffect, useRef, useState } from 'react'
import ChartStreaming from '@robloche/chartjs-plugin-streaming'

export const COMPONENT_TYPE =
    'mussonindustrial.chart.chart-js.tag-history-chart'
export type ChartJSComponentProps = ChartProps

ChartJS.register(ChartStreaming)

export function TagHistoryChartComponent(
    props: ComponentProps<ChartJSComponentProps>
) {
    const [previousValues, setPreviousValues] = useState<number[]>([])
    const chartRef = useRef<ChartJS>(null)

    // Subscribe to value changes
    useEffect(() => {
        return props.store.props.subscribe((tree: PropertyTree) => {
            const currentValues = tree.readArray('values', [])
            let modified = false
            for (let i = 0; i < currentValues.length; i++) {
                if (currentValues[i] !== previousValues[i]) {
                    chartRef.current?.config.data.datasets[i].data.push({
                        x: Date.now(),
                        y: currentValues[i],
                    })
                    modified = true
                }
                previousValues[i] = currentValues[i]
                setPreviousValues(previousValues)
            }
            if (modified) {
                props.store.props.write('data', chartRef.current?.config.data)
            }
            chartRef.current?.update('quiet')
        })
    }, [])

    const [data] = useState({
        datasets: [
            {
                label: 'Dataset 1',

                fill: false,
                lineTension: 0.4,
                backgroundColor: '#f44336',
                borderColor: '#f44336',
                borderJoinStyle: 'miter' as const,
                pointRadius: 0,
                showLine: true,
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                data: [] as any,
            },
        ],
    })

    return (
        <div {...props.emit()}>
            <Chart
                type={props.store.props.read('type')}
                options={props.store.props.read('options')}
                data={data}
                ref={chartRef}
            />
        </div>
    )
}

export class TagHistoryChartComponentMeta implements ComponentMeta {
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
        return TagHistoryChartComponent as PComponent
    }
}
