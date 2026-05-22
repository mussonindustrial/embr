import { merge } from 'lodash'
import { getClientStore } from '../utils'
import {
  AbstractUIElementStore,
  ClientStore,
  PageStore,
  ViewStore,
} from '@inductiveautomation/perspective-client'
import { createViewFunction, makeSendMessage } from '../scripting'

export type GlobalsNamespace = Record<string, unknown>
export type CallingContext = {
  client?: ClientStore
  page?: PageStore
  view?: ViewStore
  component?: AbstractUIElementStore
}
export type ScriptingGlobals = Record<string, GlobalsNamespace>

export interface ScriptingStore {
  /**
   * Add a global to a namespace.
   * @param namespace
   * @param name
   * @param value
   */
  add(namespace: string, name: string, value: unknown): void

  /**
   * Create a globals set for a calling context.
   * @param context
   */
  createGlobals(context: CallingContext): ScriptingGlobals
}

export class ScriptingStoreImpl implements ScriptingStore {
  private globals = new Map<string, GlobalsNamespace>()

  add(namespace: string, name: string, value: unknown) {
    const existing = this.globals.get(namespace) ?? {}
    merge(existing, { [name]: value })
    this.globals.set(namespace, existing)
  }

  createGlobals(context: CallingContext): ScriptingGlobals {
    if (context.client == undefined) {
      context.client = getClientStore()
    }

    const globals = {
      perspective: {
        context,
        sendMessage: makeSendMessage(context),
        createView: createViewFunction(context),
      },
    }

    merge(globals, Object.fromEntries(this.globals))
    return globals
  }
}
