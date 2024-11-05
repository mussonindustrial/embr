import EventSource from 'eventsource'
import { AuthRequest, SessionInfo } from './types'

import fetchBuilder from 'fetch-retry'
const fetchRetry = fetchBuilder(fetch)

type EventStreamClientOptions = {
    url: string
}

type EventStreamConfiguration = Record<string, unknown>

type SubscribeResponse = {
    status: string
    data: SessionInfo
}

type EventListener = (event: MessageEvent) => void

export class EventStreamClient {
    streams: EventStreamConfiguration
    auth: AuthRequest
    options: EventStreamClientOptions

    eventSource?: EventSource
    sessionInfo?: SessionInfo

    eventListeners: Record<string, Set<EventListener>> = {}

    constructor(
        streams: EventStreamConfiguration,
        auth: AuthRequest,
        options: Partial<EventStreamClientOptions>
    ) {
        this.streams = streams
        this.auth = auth
        this.options = {
            url: options.url ? options.url : '/embr/event-stream/session',
        }
    }

    async connect() {
        this.close()

        const requestBody = JSON.stringify({
            auth: this.auth,
            streams: this.streams,
        })

        const response = await fetchRetry(this.options.url, {
            method: 'POST',
            mode: 'cors',
            body: requestBody,
        })
        const subscribe = (await response.json()) as SubscribeResponse
        this.sessionInfo = subscribe.data

        const eventSourceUrl = `${this.options.url}/${this.sessionInfo.session_id}`
        this.eventSource = new EventSource(eventSourceUrl)

        for (const type in this.eventListeners) {
            const listeners = this.eventListeners[type]
            listeners.forEach((listener) =>
                this.eventSource?.addEventListener(type, listener)
            )
        }
    }

    close() {
        this.eventSource?.close()
        this.eventSource = undefined
        this.sessionInfo = undefined
    }

    addEventListener(type: string, listener: EventListener): void {
        const typeListeners = this.eventListeners[type]
        if (typeListeners) {
            typeListeners.add(listener)
        } else {
            this.eventListeners[type] = new Set()
            this.eventListeners[type].add(listener)
        }
        this.eventSource?.addEventListener(type, listener)
    }

    removeEventListener(type: string, listener: EventListener): boolean {
        const typeListeners = this.eventListeners[type]
        if (typeListeners) {
            return typeListeners.delete(listener)
        }
        this.eventSource?.removeEventListener(type, listener)
        return false
    }
}
