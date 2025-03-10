import {
  AbstractUIElementStore,
  ClientStore,
  PageStore,
  ViewStore,
} from '@inductiveautomation/perspective-client'
import { getClientStore } from '../utils'
import { makeSendMessage, SendMessage } from './sendMessage'

export type CallingContext = {
  client?: ClientStore
  page?: PageStore
  view?: ViewStore
  component?: AbstractUIElementStore
}

export type ScriptingGlobals = {
  perspective: {
    context: CallingContext
    sendMessage: SendMessage
  }
}

export function createScriptingGlobals(
  context: CallingContext
): ScriptingGlobals {
  if (context.client == undefined) {
    context.client = getClientStore()
  }

  return {
    perspective: {
      context,
      sendMessage: makeSendMessage(context),
    },
  }
}
