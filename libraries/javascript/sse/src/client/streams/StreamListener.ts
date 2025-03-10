import { EventStreamClient } from '../EventStreamClient'

export class StreamListener {
  client: EventStreamClient
  constructor(client: EventStreamClient) {
    this.client = client
  }
}
