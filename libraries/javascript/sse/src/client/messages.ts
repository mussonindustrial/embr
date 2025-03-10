import { SessionInfo } from './types'

type MessageParser<T> = (message: string, session: SessionInfo) => T

class MessageTypeRegistry<T> {
  private constructor(private registry: T) {}
  register<K extends string, S>(
    key: K,
    parser: MessageParser<S>
  ): MessageTypeRegistry<Record<K, MessageParser<S>> & T> {
    // add service to registry and return the same object with a narrowed type
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ;(this.registry as any)[key] = parser
    return this as MessageTypeRegistry<Record<K, MessageParser<S>> & T>
  }
  get<K extends keyof T>(key: K): T[K] {
    return this.registry[key]
  }
  static init(): MessageTypeRegistry<object> {
    return new MessageTypeRegistry({})
  }
}

export const MessageParsers = MessageTypeRegistry.init()
  .register('tag_change', () => 5)
  .register('tag_alarm', () => 'test')
