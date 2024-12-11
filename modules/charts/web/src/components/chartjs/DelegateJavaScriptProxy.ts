import {
  ComponentStoreDelegate,
  JsObject,
} from '@inductiveautomation/perspective-client'
import { toFunction } from '@embr-js/utils'

const EVENTS = {
  JS_ERROR: 'js-error',
  JS_RESOLVE: 'js-resolve',
  JS_RUN: 'js-run',
}

export type JavaScriptRunEvent = {
  id: string
  function: string
  args: any
}

export class DelegateJavaScriptProxy {
  private delegate: ComponentStoreDelegate
  private globals: JsObject

  constructor(delegate: ComponentStoreDelegate, globals: JsObject) {
    this.delegate = delegate
    this.globals = globals
  }

  handles(eventName: string) {
    return eventName == 'js-run'
  }

  setGlobals(globals: JsObject) {
    this.globals = globals
  }

  private resolveSuccess(id: string, data: unknown) {
    console.log('resolveSuccess', data)
    this.delegate.fireEvent(EVENTS.JS_RESOLVE, {
      id,
      success: true,
      data,
    })
  }

  private resolveError(id: string, error: unknown) {
    console.log('resolveError', error)
    const errorData =
      error instanceof Error
        ? {
            name: error.name,
            message: error.message,
            stack: error.stack,
          }
        : error

    this.delegate.fireEvent(EVENTS.JS_ERROR, {
      id,
      success: false,
      error: errorData,
    })

    throw error
  }

  run(id: string, block: () => any) {
    new Promise((resolve) => {
      resolve(block())
    })
      .then((result: unknown) => this.resolveSuccess(id, result))
      .catch((error: unknown) => this.resolveError(id, error))
  }

  handleEvent(event: JavaScriptRunEvent) {
    this.run(event.id, () => {
      const f = toFunction(event.function, this.globals)
      return event.args !== undefined ? f(event.args) : f()
    })
  }
}
