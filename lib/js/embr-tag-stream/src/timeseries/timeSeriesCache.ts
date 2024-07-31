export type TimeSeriesEntry = {
    timestamp: EpochTimeStamp
    value: number
}

export function newTimeSeriesCache(data?: TimeSeriesEntry[]) {
    return new TimeSeriesCache(data ?? [])
}

export class TimeSeriesCache {
    data: TimeSeriesEntry[] = []

    constructor(data: TimeSeriesEntry[]) {
        this.data = data
    }

    get length() {
        return this.data.length
    }

    get start() {
        return this.data[0].timestamp
    }

    get end() {
        return this.data[this.length - 1].timestamp
    }

    [Symbol.iterator]() {
        let index = -1

        return {
            next: () => ({
                value: this.data[++index],
                done: !(index in this.data),
            }),
        }
    }

    static of(data: TimeSeriesEntry[]): TimeSeriesCache {
        return new TimeSeriesCache(data)
    }

    /**
     * Return the average milliseconds between points.
     * */
    getAveragePeriod(): number {
        const span = this.end - this.start
        return span / this.length
    }

    guessArrayLocation(timestamp: EpochTimeStamp): number {
        const period = this.getAveragePeriod()
        const span = timestamp - this.start
        return Math.max(Math.min(Math.trunc(span / period), this.length - 1), 0)
    }

    findIndexBefore(timestamp: EpochTimeStamp): number {
        if (timestamp >= this.end) {
            return this.length - 1
        }
        if (timestamp <= this.start) {
            return -1
        }

        const guess = this.guessArrayLocation(timestamp)
        const value = this.data[guess]

        if (value.timestamp > timestamp) {
            for (let index = guess; index >= 0; index--) {
                if (this.data[index].timestamp <= timestamp) {
                    return index
                }
            }
        } else {
            for (let index = guess; index < this.length; index++) {
                if (this.data[index].timestamp >= timestamp) {
                    return index - 1
                }
            }
        }
        return -1
    }

    getView(start: EpochTimeStamp, end: EpochTimeStamp): TimeSeriesEntry[] {
        const index_start = this.findIndexBefore(start) + 1
        const index_end = this.findIndexBefore(end) + 1

        return this.data.slice(index_start, index_end)
    }

    append(data: TimeSeriesEntry[]) {
        // this.data.push.apply(data)
        for (let index = 0; index < data.length; index++) {
            this.data.push(data[index])
        }
    }

    prepend(data: TimeSeriesEntry[]) {
        this.data.unshift(...data)
    }

    /**
     * Re-index the cache, sorting the values by timestamp.
     * This is expensive, and should be avoided when possible.
     */
    reindex() {
        this.data.sort((a, b) => {
            return a.timestamp - b.timestamp
        })
    }

    merge(data: TimeSeriesCache) {
        if (data.end < this.start) {
            this.prepend(data.data)
        } else if (this.end < data.start) {
            this.append(data.data)
        } else if (data.end > this.end && data.start < this.start) {
            this.data = data.data
        } else if (data.end > this.start && data.start < this.start) {
            const view = data.getView(0, this.start)
            this.prepend(view)
        } else if (data.end > this.end && data.start < this.end) {
            const view = data.getView(this.end, data.end)
            this.append(view)
        }
    }
}
