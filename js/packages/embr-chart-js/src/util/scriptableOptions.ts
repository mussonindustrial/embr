import { isFunction, toFunction } from '@mussonindustrial/embr-utils'

export type ChartScript = (chart: unknown) => void
export type ContextScript = (context: unknown, options: unknown) => unknown
export type ScriptableOption<T> = T | unknown

export function asChartScript(
    property: unknown,
    extraContext: object = {}
): ScriptableOption<ChartScript> {
    if (typeof property === 'string' && isFunction(property)) {
        return (chart: unknown) =>
            toFunction(property)({ chart, ...extraContext })
    }
    return property
}

export function asContextScript(
    property: unknown,
    extraContext: object = {}
): ScriptableOption<ContextScript> {
    if (typeof property === 'string' && isFunction(property)) {
        return (context: unknown, options: unknown) =>
            toFunction(property)({ context, options, ...extraContext })
    }
    return property
}
