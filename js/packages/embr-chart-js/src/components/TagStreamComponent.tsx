import React, { MutableRefObject, useEffect, useRef, useState } from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { transformProps } from '@mussonindustrial/embr-utils'
import { BufferedTagStreamClient } from '@mussonindustrial/embr-tag-stream'
import { Chart as Chartjs, ChartProps } from 'react-chartjs-2'
import { Chart } from 'chart.js'
import { cloneDeep, unset } from 'lodash'
import { getCSSTransform, getScriptTransform } from '../util'

export const TagStreamComponentComponentMeta: ComponentMeta = {
    getComponentType: function (): string {
        return 'embr.chart.tag-stream'
    },
    getDefaultSize: function (): SizeObject {
        return {
            width: 300,
            height: 300,
        }
    },
    getViewComponent: function (): PComponent {
        return TagStreamComponent as PComponent
    },
    getPropsReducer(tree: PropertyTree): TagStreamChartProps {
        return {
            tags: tree.readArray('tags'),
            type: tree.readString('type'),
            options: tree.read('options', {}),
            data: tree.read('data', {}),
            plugins: tree.readArray('plugins', []),
            events: tree.read('events', {}),
        } as never
    },
}

type TagStreamChart = Chart
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type TagStreamChartData = any[]
type ChartEvent = (chart: TagStreamChart | undefined) => void
type TagStreamChartEvents = {
    beforeRender?: ChartEvent
}
type TagStreamChartProps = ChartProps & {
    tags?: string[]
    events?: TagStreamChartEvents
}

function callUserChartEvent(
    chart: TagStreamChart | undefined,
    props: TagStreamChartProps,
    event: keyof TagStreamChartEvents
) {
    if (chart === undefined || props.events === undefined) {
        return
    }

    const userFunction = props.events[event]
    if (userFunction !== undefined && typeof userFunction == 'function') {
        userFunction(chart)
    }
}

function extractPropsData(props: TagStreamChartProps) {
    const localProps = cloneDeep(props)
    const data: TagStreamChartData[] = []

    localProps.data.datasets.forEach((dataset) => {
        data.push(dataset.data)
        unset(dataset, 'data')
    })
    return { props, data }
}

function installPropsData(
    props: TagStreamChartProps,
    data: TagStreamChartData[]
) {
    data.forEach((data, index) => {
        props.data.datasets[index].data = data
    })
}

export function TagStreamComponent(props: ComponentProps<TagStreamChartProps>) {
    const chartRef: MutableRefObject<TagStreamChart | undefined> =
        useRef(undefined)

    const client = useRef<BufferedTagStreamClient>(
        new BufferedTagStreamClient(undefined, {
            type: 'perspective',
            session_id: window.__client!.sessionId!,
        })
    )
    const chartData = useRef<TagStreamChartData[]>([])

    const [transformedProps, setTransformedProps] =
        useState<TagStreamChartProps>(props.props)

    useEffect(() => {
        setTransformedProps(() => {
            const { props: configProps } = extractPropsData(props.props)
            const newTransformedProps = transformProps(configProps, [
                getScriptTransform({ self: props, client: window.__client }),
                getCSSTransform(chartRef.current?.canvas.parentElement),
            ]) as TagStreamChartProps

            installPropsData(newTransformedProps, chartData.current)
            callUserChartEvent(
                chartRef.current,
                newTransformedProps,
                'beforeRender'
            )
            return newTransformedProps
        })
    }, [props.props])

    useEffect(() => {
        const refreshData = setInterval(() => {
            if (!client.current) return

            const newData = client.current.read()
            newData.forEach((data, index) => {
                data.forEach((changeData) => {
                    chartRef.current?.data.datasets[index].data.push({
                        x: changeData.timestamp,
                        y: changeData.value,
                    })
                })
            })
            chartRef.current?.update('quiet')
        }, 1000)

        return () => {
            clearInterval(refreshData)
            client.current?.close()
        }
    }, [])

    useEffect(() => {
        client.current.close()
        if (props.props.tags) {
            client.current.subscribe(props.props.tags)
        }
    }, [props.props.tags])

    return (
        <div {...props.emit()}>
            <Chartjs
                type={transformedProps.type}
                options={transformedProps.options}
                data={transformedProps.data}
                plugins={transformedProps.plugins}
                ref={chartRef}
                updateMode="quiet"
            />
        </div>
    )
}
