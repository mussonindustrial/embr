import { TagStreamClient } from './TagStreamClient'
import { TagChangeCallback, TagChangeData, TagChangeEvent } from './types'

export type TagChangeEventLog = TagChangeData[]
export type TagChangeBuffer = TagChangeEventLog[]

export class BufferedTagStreamClient extends TagStreamClient {
    buffer: TagChangeBuffer = []

    read() {
        const bufferSnapshot = structuredClone(this.buffer)
        this.clear()
        return bufferSnapshot
    }

    clear() {
        this.buffer = Array(this.sessionInfo?.streams?.tag?.tags.length)
            .fill(undefined)
            .map(() => [])
    }

    async subscribe(tags: string[]) {
        this.buffer = Array(tags.length)
            .fill(undefined)
            .map(() => [])
        await super.subscribe(tags)
    }

    protected _onTagChange = (event: TagChangeEvent) => {
        this.addToBuffer(event)
        this._userOnTagChange && this._userOnTagChange(event)
    }

    private addToBuffer(event: TagChangeEvent) {
        const b = this.buffer[event.tag.id]
        if (b instanceof Array) {
            b.push(event.data)
        } else {
            this.buffer[event.tag.id] = [event.data]
        }
    }

    protected _userOnTagChange?: TagChangeCallback
    override onTagChange(callback: TagChangeCallback) {
        this._userOnTagChange = callback
    }
}
