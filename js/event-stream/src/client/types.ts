export type TagConfig = {
    id: number
    path: string
}

export type TagEmitter = {
    events: TagEventType[]
    tags: TagConfig[]
}

export type TagEventType = 'tag_change' | 'tag_alarm'

export type SessionInfo = {
    session_id: string
    streams: {
        tag?: TagEmitter
    }
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

export type AuthRequest =
    | AnonymousAuthRequest
    | BasicAuthRequest
    | PerspectiveAuthRequest
export type AnonymousAuthRequest = {
    type: 'anonymous'
}
export type BasicAuthRequest = {
    type: 'basic'
}
export type PerspectiveAuthRequest = {
    type: 'perspective'
    session_id: string
}
