import { TimeSeriesCache } from './timeSeriesCache'

export class TagCache {
  tags: Record<string, TimeSeriesCache> = {}
}
