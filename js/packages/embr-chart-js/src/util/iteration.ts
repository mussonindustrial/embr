import isDate from 'lodash/isDate'

export function zip<S1, S2>(
    firstCollection: Array<S1>,
    lastCollection: Array<S2>
): Array<[S1, S2]> {
    const length = Math.min(firstCollection.length, lastCollection.length)
    const zipped: Array<[S1, S2]> = []

    for (let index = 0; index < length; index++) {
        zipped.push([firstCollection[index], lastCollection[index]])
    }

    return zipped
}

export function zipShort<S1, S2>(
    firstCollection: Array<S1>,
    lastCollection: Array<S2>
): Array<[S1, S2]> {
    const length = Math.min(firstCollection.length, lastCollection.length)
    const zipped: Array<[S1, S2]> = []

    for (let index = 0; index < length; index++) {
        zipped.push([firstCollection[index], lastCollection[index]])
    }

    return zipped
}

export function zipLong<S1, S2>(
    firstCollection: Array<S1>,
    lastCollection: Array<S2>
): Array<[S1, S2]> {
    const length = Math.max(firstCollection.length, lastCollection.length)
    const zipped: Array<[S1, S2]> = []

    for (let index = 0; index < length; index++) {
        zipped.push([firstCollection[index], lastCollection[index]])
    }

    return zipped
}

export function recursiveMap<T>(
    obj: T,
    callback: (obj: unknown) => unknown
): unknown {
    if (isDate(obj)) return obj
    if (Array.isArray(obj)) return obj.map((v) => recursiveMap(v, callback))
    if (obj && typeof obj === 'object')
        return Object.fromEntries(
            Object.entries(obj).map(([k, v]) => [k, recursiveMap(v, callback)])
        )
    return callback(obj)
}
