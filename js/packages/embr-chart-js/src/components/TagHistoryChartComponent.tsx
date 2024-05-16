import * as React from 'react'
import {
    AbstractUIElementStore,
    ComponentMeta,
    ComponentProps,
    ComponentStoreDelegate,
    JsObject,
    PComponent,
    PropertyTree,
    SizeObject,
    makeLogger,
} from '@inductiveautomation/perspective-client'
import _ from 'lodash'
import 'chartjs-adapter-luxon'
import { Chart, ChartProps } from 'react-chartjs-2'

export const COMPONENT_TYPE = 'mussonindustrial.chart.chart-js.taghistory'
const logger = makeLogger(COMPONENT_TYPE)

export type PenConfig = {
    tagPath: number
}

export type DataPoint = {
    x: number
    y: number
}

export type RealtimeData = DataPoint[]

export type TagHistoryChartProps = ChartProps & {
    tags: PenConfig[]
}

enum TagEvents {
    MESSAGE_DATA_NEW = 'tag-history-chart-data-new',
}

export type TagHistoryDataMessage = {
    values: number[]
}

type ChartReference = typeof Chart | null

type TagHistoryDelegateState = {
    chart: ChartReference
    setChart: (chart: ChartReference) => void
    data: RealtimeData[]
}

export class TagHistoryGatewayDelegate extends ComponentStoreDelegate {
    public state: TagHistoryDelegateState = {
        setChart: (chart) => (this.state.chart = chart),
        chart: null,
        data: [],
    }

    mapStateToProps(): TagHistoryDelegateState {
        return this.state
    }

    public dataReceived(eventObject: TagHistoryDataMessage): void {
        const now = Date.now()

        eventObject.values.forEach((newValue, index) => {
            if (!this.state.data[index]) {
                this.state.data[index] = []
            }
            this.state.data[index].push({
                x: now,
                y: newValue,
            })
        })

        if (typeof this.state.chart !== 'function') {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ;(this.state.chart as any).update('quiet')
        }
    }

    handleEvent(eventName: string, eventObject: JsObject): void {
        switch (eventName) {
            case TagEvents.MESSAGE_DATA_NEW:
                this.dataReceived(eventObject as TagHistoryDataMessage)
                break
            default:
                logger.warn(
                    () =>
                        `No delegate event handler found for event: ${eventName} in TagHistoryChartComponentGatewayDelegate`
                )
        }
    }
}

export function TagHistoryChartComponent(
    props: ComponentProps<TagHistoryChartProps, TagHistoryDelegateState>
) {
    const chartRef = React.useRef<typeof Chart>(null)
    if (props.delegate) {
        props.delegate.setChart(chartRef.current)
    }

    const data = props.props.data
    ;(props.store.delegate as TagHistoryGatewayDelegate).state.data.forEach(
        (tagHistory, index) => {
            if (data.datasets[index]) {
                data.datasets[index].data = tagHistory
            }
        }
    )

    const options = _.merge(props.props.options, {
        scales: {
            x: {
                type: 'realtime',
            },
        },
    })

    return (
        <div {...props.emit()}>
            <Chart
                type={props.props.type}
                options={options}
                data={data}
                ref={chartRef as never}
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

    createDelegate(
        component: AbstractUIElementStore
    ): ComponentStoreDelegate | undefined {
        return new TagHistoryGatewayDelegate(component)
    }

    getPropsReducer(tree: PropertyTree): TagHistoryChartProps {
        return {
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
            tags: tree.readArray('tags', []),
        } as never
    }

    getViewComponent(): PComponent {
        return TagHistoryChartComponent as PComponent
    }
}
