import { JsObject } from '@inductiveautomation/perspective-client'
import { CallingContext } from './ScriptingGlobals'

export const PROTOCOL = {
  SEND_MESSAGE: 'send-message',
}

export type SendMessage = (
  type: string,
  payload: JsObject,
  scope?: MessageScope
) => void

export type MessageScope = 'view' | 'page' | 'session'
const MessageScopes = ['view', 'page', 'session']

export function makeSendMessage(context: CallingContext): SendMessage {
  return (type: string, payload: JsObject, scope = 'page') => {
    if (context.client === undefined) {
      console.warn(
        'Cannot send message, current context does not contain a client store.'
      )
      return
    }

    if (!MessageScopes.includes(scope)) {
      console.warn(
        `Cannot send message, scope ${scope} is not valid. Valid scopes include: ${MessageScopes}`
      )
      return
    }

    context.client.connection.send(PROTOCOL.SEND_MESSAGE, {
      type,
      payload,
      scope,
    })
  }
}
