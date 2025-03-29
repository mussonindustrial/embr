import {
  AbstractUIElementStore,
  ClientStore,
  JsObject,
  PageStore,
  ViewStore,
} from '@inductiveautomation/perspective-client'
import { getClientStore, getGlobals } from '../utils'
import { makeSendMessage, SendMessage } from './sendMessage'
import addGlobals from '../utils/addGlobal'
import { createRenderView, RenderView } from './renderView'

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
    view: {
      render: RenderView
    }
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
      view: {
        render: createRenderView(context),
      },
      ...getGlobals().scriptingGlobals,
    },
  }
}

export function addScriptingGlobals(globals: JsObject) {
  addGlobals({ scriptingGlobals: globals })
}
