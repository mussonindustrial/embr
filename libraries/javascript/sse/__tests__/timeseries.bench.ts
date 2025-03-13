import { bench, describe } from 'vitest'
import { newTimeSeriesCache, generateTimeSeries } from '../src/'

const original_data_a = generateTimeSeries(
  new Date('2024-01-01 00:00:00').getTime(),
  new Date('2025-01-01 00:00:00').getTime(),
  100000
)
const original_data_b = generateTimeSeries(
  new Date('2023-06-01 00:00:00').getTime(),
  new Date('2024-06-01 00:00:00').getTime(),
  100000
)
const original_data_c = generateTimeSeries(
  new Date('2023-04-01 00:00:00').getTime(),
  new Date('2023-05-01 00:00:00').getTime(),
  100000
)
const original_newData = generateTimeSeries(
  new Date('2023-04-01 00:00:00').getTime(),
  new Date('2023-05-01 00:00:00').getTime(),
  100
)

const data_a = newTimeSeriesCache([])
const data_b = newTimeSeriesCache([])
const data_c = newTimeSeriesCache([])
const newData = newTimeSeriesCache([])

const setup = (task) => {
  task.opts.beforeEach = () => {
    Object.assign(data_a, original_data_a)
    Object.assign(data_b, original_data_b)
    Object.assign(data_c, original_data_c)
    Object.assign(newData, original_newData)
  }
}

describe('time-series', () => {
  bench(
    'iterate through all dates',
    () => {
      for (let index = 0; index < data_a.length; index++) {
        /* empty */
      }
    },
    { setup }
  )

  bench(
    'add data before',
    () => {
      data_b.merge(data_c)
    },
    { setup }
  )

  bench(
    'add data after',
    () => {
      data_c.merge(data_b)
    },
    { setup }
  )

  bench(
    'add data somewhere before',
    () => {
      data_a.merge(data_b)
    },
    { setup }
  )

  bench(
    'add data somewhere after',
    () => {
      data_b.merge(data_a)
    },
    { setup }
  )

  bench(
    'append',
    () => {
      data_b.append(newData.data)
    },
    { setup }
  )

  bench(
    'prepend',
    () => {
      data_b.prepend(newData.data)
    },
    { setup }
  )

  bench(
    'get a view',
    () => {
      data_b.getView(
        Date.parse('2024-01-01 00:00:00'),
        Date.parse('2024-04-01 00:00:00')
      )
    },
    { setup }
  )
})
