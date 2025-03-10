import { EventStreamClient } from '../EventStreamClient'
import { StreamListener } from './StreamListener'
import { EventCallback } from './types'

type RawTagChangeData = {
  tag_id: number
  q: number
  v: number
  t: number
}

type RawHistoryStartMessage = {
  message_id: number
  size: number
}

type RawHistoryDataMessage = {
  message_id: number
  tag_id: number
  t: number
  v: unknown
}

type RawHistoryEndMessage = {
  message_id: number
}

type TagEvent<T> = {
  data: T
  tag: {
    id: number
    path: string
  }
}

export type TagChangeEvent2 = TagEvent<{
  value: unknown
  quality: number
  timestamp: number
}>
export type TagAlarmEvent = TagEvent<{
  value: unknown
}>

export type TagHistoryValue = {
  value: unknown
  timestamp: number
}
export type TagHistoryEvent = TagEvent<TagHistoryValue>
export type TagHistoryBlock = TagEvent<TagHistoryValue[]>

export class TagStreamListener extends StreamListener {
  tagHistoryCache: Record<string, Array<Array<TagHistoryValue>>> = {}

  constructor(client: EventStreamClient) {
    super(client)
    client.addEventListener('tag_change', this.tagChange.bind(this))
    client.addEventListener('tag_alarm', this.tagAlarm.bind(this))

    client.addEventListener('tag_history_start', this.historyStart.bind(this))
    client.addEventListener('tag_history', this.historyData.bind(this))
    client.addEventListener('tag_history_end', this.HistoryEnd.bind(this))
  }

  get config() {
    return this.client.sessionInfo?.streams?.tag
  }

  getTagConfig(tag_id: number) {
    return this.config?.tags[tag_id]
  }

  private tagChange(event: MessageEvent) {
    const message = JSON.parse(event.data) as RawTagChangeData
    const tag = this.getTagConfig(message.tag_id)!

    const parsed = {
      data: {
        value: message.v,
        quality: message.q,
        timestamp: message.t,
      },
      tag,
    } as TagChangeEvent2
    this._onTagChange && this._onTagChange(parsed)
  }

  private tagAlarm(event: MessageEvent) {
    const message = JSON.parse(event.data) as RawTagChangeData
    const tag = this.getTagConfig(message.tag_id)!

    const parsed = {
      tag,
      data: {
        value: message.v,
      },
    } as TagChangeEvent2

    this._onTagAlarm && this._onTagAlarm(parsed)
  }

  private historyStart(event: MessageEvent): void {
    const message = JSON.parse(event.data) as RawHistoryStartMessage

    const tagCount = this.config?.tags.length
    if (tagCount) {
      this.tagHistoryCache[message.message_id] = []
      for (let tag_id = 0; tag_id < tagCount; tag_id++) {
        this.tagHistoryCache[message.message_id][tag_id] = []
      }
    }
  }
  private historyData(event: MessageEvent): void {
    const message = JSON.parse(event.data) as RawHistoryDataMessage
    const tag = this.getTagConfig(message.tag_id)!

    const data = {
      value: message.v,
      timestamp: message.t,
    }

    this.tagHistoryCache[message.message_id][message.tag_id].push(data)

    const newEvent = {
      tag,
      data,
    } as TagHistoryEvent
    this._onHistoryStreamed && this._onHistoryStreamed(newEvent)
  }
  private HistoryEnd(event: MessageEvent): void {
    const message = JSON.parse(event.data) as RawHistoryEndMessage

    const tagCount = this.config?.tags.length
    if (tagCount) {
      for (let tag_id = 0; tag_id < tagCount; tag_id++) {
        const newEvent = {
          tag: this.getTagConfig(tag_id)!,
          data: this.tagHistoryCache[message.message_id][tag_id],
        } as TagHistoryBlock
        this._onHistoryComplete && this._onHistoryComplete(newEvent)
      }
    }
    delete this.tagHistoryCache[message.message_id]
  }

  protected _onTagChange?: EventCallback<TagChangeEvent2>
  onTagChange(callback: EventCallback<TagChangeEvent2>) {
    this._onTagChange = callback
  }

  protected _onTagAlarm?: EventCallback<TagAlarmEvent>
  onTagAlarm(callback: EventCallback<TagAlarmEvent>) {
    this._onTagAlarm = callback
  }

  protected _onHistoryStreamed?: EventCallback<TagHistoryEvent>
  onHistoryStreamed(callback: EventCallback<TagHistoryEvent>) {
    this._onHistoryStreamed = callback
  }

  protected _onHistoryComplete?: EventCallback<TagHistoryBlock>
  onHistoryComplete(callback: EventCallback<TagHistoryBlock>) {
    this._onHistoryComplete = callback
  }
}
