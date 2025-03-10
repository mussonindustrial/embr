import {
  AlarmEventCallback,
  AlarmEventData,
  AuthRequest,
  EventCallback,
  RawTagChangeData,
  SessionInfo,
  TagChangeCallback,
  TagChangeData,
} from './types'

type SubscribeResponse = {
  status: string
  data: SessionInfo
}

export class TagStreamClient {
  url: string
  eventSource?: EventSource
  sessionInfo?: SessionInfo
  auth?: AuthRequest

  constructor(url?: string, auth?: AuthRequest) {
    this.auth = auth
    if (url === undefined) {
      this.url = '/embr/event-stream/session'
    } else {
      this.url = url
    }
  }

  protected _onError?: EventCallback
  onError(callback: EventCallback) {
    this._onError = callback
  }

  protected _onOpen?: EventCallback
  onOpen(callback: EventCallback) {
    this._onOpen = callback
  }

  protected _onTagChange?: TagChangeCallback
  onTagChange(callback: TagChangeCallback) {
    this._onTagChange = callback
  }

  protected _onAlarmEvent?: AlarmEventCallback
  onAlarmEvent(callback: AlarmEventCallback) {
    this._onAlarmEvent = callback
  }

  getTagConfig(tag_id: number) {
    return this.sessionInfo?.streams?.tag?.tags[tag_id]
  }

  async subscribe(tags: string[]) {
    this.close()

    const requestBody = JSON.stringify({
      auth: this.auth,
      streams: {
        tag: {
          paths: tags,
          events: ['tag_change', 'tag_alarm'],
        },
      },
    })

    const response = await fetch(this.url, {
      method: 'POST',
      mode: 'cors',
      body: requestBody,
    })
    const subscribe = (await response.json()) as SubscribeResponse
    this.sessionInfo = subscribe.data

    const url = `${this.url}/${this.sessionInfo.session_id}`

    this.eventSource = new EventSource(url)

    this.eventSource.onopen = (event) => this._onOpen && this._onOpen(event)
    this.eventSource.onerror = (event) => this._onError && this._onError(event)

    this.eventSource.addEventListener('tag_change', (event) => {
      const message = JSON.parse(event.data) as RawTagChangeData
      const tag = this.getTagConfig(message.tag_id)!
      const data = {
        value: message.v,
        quality: message.q,
        timestamp: message.t,
      } as TagChangeData
      this._onTagChange && this._onTagChange({ tag, data })
    })

    this.eventSource.addEventListener('tag_alarm', (event) => {
      const data = JSON.parse(event.data) as AlarmEventData
      const tag = this.getTagConfig(data.tag_id)!
      this._onAlarmEvent && this._onAlarmEvent({ tag, data })
    })
  }

  close() {
    this.eventSource?.close()
  }
}
