import { it, expect, describe } from 'vitest'
import { generateTimeSeries, newTimeSeriesCache } from '../src/'

describe('time-series', () => {
    it('should create a time series data object', async () => {
        const data = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-12 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-12 06:00:00'), value: 5 },
        ])

        expect(data.length).toBe(2)
        expect(data.start).toStrictEqual(Date.parse('2024-06-12 00:00:00'))
        expect(data.end).toStrictEqual(Date.parse('2024-06-12 06:00:00'))
    })

    it('should add data before', async () => {
        const data = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-12 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-12 06:00:00'), value: 5 },
        ])
        const dataNew = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-11 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-11 06:00:00'), value: 5 },
        ])
        data.merge(dataNew)

        expect(data.length).toBe(4)
        expect(data.start).toStrictEqual(Date.parse('2024-06-11 00:00:00'))
        expect(data.end).toStrictEqual(Date.parse('2024-06-12 06:00:00'))
    })

    it('should add data after', async () => {
        const data = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-11 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-11 06:00:00'), value: 5 },
        ])
        const dataNew = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-12 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-12 06:00:00'), value: 5 },
        ])
        data.merge(dataNew)

        expect(data.length).toBe(4)
        expect(data.start).toStrictEqual(Date.parse('2024-06-11 00:00:00'))
        expect(data.end).toStrictEqual(Date.parse('2024-06-12 06:00:00'))
    })

    it('should add mixed data before', async () => {
        const data = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-11 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-12 00:00:00'), value: 5 },
        ])
        const dataNew = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-10 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-11 06:00:00'), value: 5 },
        ])
        data.merge(dataNew)

        expect(data.length).toBe(3)
        expect(data.start).toStrictEqual(Date.parse('2024-06-10 00:00:00'))
        expect(data.end).toStrictEqual(Date.parse('2024-06-12 00:00:00'))
    })

    it('should add mixed data after', async () => {
        const data = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-11 00:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-12 00:00:00'), value: 5 },
        ])
        const dataNew = newTimeSeriesCache([
            { timestamp: Date.parse('2024-06-11 06:00:00'), value: 5 },
            { timestamp: Date.parse('2024-06-13 00:00:00'), value: 5 },
        ])
        data.merge(dataNew)

        expect(data.length).toBe(3)
        expect(data.start).toStrictEqual(Date.parse('2024-06-11 00:00:00'))
        expect(data.end).toStrictEqual(Date.parse('2024-06-13 00:00:00'))
    })

    it('should get a view', async () => {
        const data = generateTimeSeries(
            Date.parse('2024-06-11 00:00:00'),
            Date.parse('2024-06-12 00:00:00'),
            100
        )

        const view = data.getView(
            Date.parse('2024-06-11 08:00:00'),
            Date.parse('2024-06-11 14:00:00')
        )

        expect(data.length).toBe(100)
        expect(data.start).toStrictEqual(Date.parse('2024-06-11 00:00:00'))
        expect(view.length).toBe(25)
    })
})
