export type TagConfig = {
    tag_id: number
    tag_path: string
    alarm_path: string
}

export type SessionInfo = {
    session_id: string
    tags: TagConfig[]
}

type SubscribeResponse = {
    status: string
    data: SessionInfo
}

export type TagChangeData = {
    quality: number
    value: number
    timestamp: number
}

export type RawTagChangeData = {
    tag_id: number
    q: number
    v: number
    t: number
}

export type TagChangeEvent = { tag: TagConfig; data: TagChangeData }
export type TagChangeCallback = (event: TagChangeEvent) => void

export type AlarmEventData = Record<string, unknown> & {
    tag_id: number
}
export type AlarmEvent = { tag: TagConfig; data: AlarmEventData }
export type AlarmEventCallback = (event: AlarmEvent) => void

export type EventCallback = (event: Event) => void

export class TagStreamClient {
    url: string
    eventSource?: EventSource
    sessionInfo?: SessionInfo
    extraBody?: Record<string, string>

    constructor(url?: string, extraBody?: Record<string, string>) {
        this.extraBody = extraBody
        if (url === undefined) {
            this.url = '/embr/tag/stream/session'
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
        return this.sessionInfo?.tags[tag_id]
    }

    async subscribe(tags: string[]) {
        this.close()

        const requestBody = JSON.stringify({
            ...this.extraBody,
            tag_paths: tags,
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
        this.eventSource.onerror = (event) =>
            this._onError && this._onError(event)

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

        this.eventSource.addEventListener('alarm_event', (event) => {
            const data = JSON.parse(event.data) as AlarmEventData
            const tag = this.getTagConfig(data.tag_id)!
            this._onAlarmEvent && this._onAlarmEvent({ tag, data })
        })
    }

    close() {
        this.eventSource?.close()
    }
}