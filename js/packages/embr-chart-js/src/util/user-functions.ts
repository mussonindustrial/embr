export type ChartScript = (chart: unknown) => void
export type ContextScript = (context: unknown, options: unknown) => unknown
export type ScriptableOption<T> = T | string

export function parseChartFunctionString(
    property: string
): ScriptableOption<ChartScript> {
    if (property.includes('return ')) {
        return new Function('chart', property) as ChartScript
    }
    return property
}

export function parseContextFunctionString(
    property: string
): ScriptableOption<ContextScript> {
    if (property.includes('return ')) {
        return new Function('context', 'options', property) as ContextScript
    }
    return property
}

export function parseCSSVariableString(
    element: Element,
    property: string
): string {
    if (property.startsWith('var(--')) {
        const propertyName = property.match(/(?<=var\()[\w-]+/)
        if (propertyName === null) {
            return property
        }

        if (element.parentElement) {
            return window
                .getComputedStyle(element.parentElement)
                .getPropertyValue(propertyName[0])
        }
        return window
            .getComputedStyle(element)
            .getPropertyValue(propertyName[0])
    }

    return property
}
