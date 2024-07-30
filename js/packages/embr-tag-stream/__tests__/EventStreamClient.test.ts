import { it, describe } from 'vitest'
import { AuthRequest, EventStreamClient, TagStreamListener } from '../src'

describe('EventStreamClient', () => {
    it('should create a client object', async () => {
        const auth: AuthRequest = { type: 'anonymous' }
        const options = {
            url: 'http://localhost:8088/embr/event-stream/session',
        }
        const client = new EventStreamClient(
            {
                tag: {
                    events: ['tag_change'],
                    paths: ['[default]FastChart/sin'],
                },
            },
            auth,
            options
        )

        const tagStream = new TagStreamListener(client)
        tagStream.onTagChange((event) => console.log(event))
        client.connect()

        const delay = (ms) => new Promise((res) => setTimeout(res, ms))
        await delay(10000)
    })
})
