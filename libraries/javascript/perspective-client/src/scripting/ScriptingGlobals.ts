import {
  AbstractUIElementStore,
  ClientStore,
  PageStore,
  ViewStore,
} from '@inductiveautomation/perspective-client'
import { getClientStore } from '../utils'
import { makeSendMessage, SendMessage } from './sendMessage'
import { merge } from 'lodash'
import { getEmbrGlobals } from '../globals'
import { CreateView, createViewFunction } from './createView'
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
    createView: CreateView
  }
}

export function createScriptingGlobals(
  context: CallingContext
): ScriptingGlobals {
  if (context.client == undefined) {
    context.client = getClientStore()
  }

  return merge(
    {
      perspective: {
        context,
        sendMessage: makeSendMessage(context),
        createView: createViewFunction(context),
      },
    },
    getEmbrGlobals().scripting.globals
  )
}
