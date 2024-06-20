import { TimeSeriesCache, TimeSeriesEntry } from '.'

export function generateTimeSeries(start: number, end: number, count: number) {
    const period = (end - start) / count
    const data: TimeSeriesEntry[] = []
    let timestamp = start
    while (timestamp < end) {
        data.push({
            timestamp,
            value: 0,
        })
        timestamp = timestamp + period
    }
    return TimeSeriesCache.of(data)
}
