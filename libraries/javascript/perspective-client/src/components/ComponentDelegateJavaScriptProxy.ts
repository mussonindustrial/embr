import {
  ComponentStoreDelegate,
  JsObject,
} from '@inductiveautomation/perspective-client'
import { toUserScript } from '@embr-js/utils'
import { createScriptingGlobals } from '../scripting'

const MESSAGES = {
  JS_ERROR: 'js-error',
  JS_RESOLVE: 'js-resolve',
  JS_RUN: 'js-run',
}

export type JavaScriptRunEvent = {
  id: string
  property: string
  function: string
  args: JsObject
}

export class ComponentDelegateJavaScriptProxy<T extends object> {
  private readonly delegate: ComponentStoreDelegate
  private ref: T | undefined

  constructor(delegate: ComponentStoreDelegate, ref?: T) {
    this.delegate = delegate
    this.ref = ref
  }

  setRef(ref?: T) {
    this.ref = ref
  }

  handles(eventName: string) {
    return eventName == MESSAGES.JS_RUN
  }

  private resolveSuccess(id: string, data: unknown) {
    this.delegate.fireEvent(MESSAGES.JS_RESOLVE, {
      id,
      success: true,
      data,
    })
  }

  private resolveError(id: string, error: unknown) {
    const errorData =
      error instanceof Error
        ? {
            name: error.name,
            message: error.message,
            stack: error.stack,
          }
        : error

    this.delegate.fireEvent(MESSAGES.JS_ERROR, {
      id,
      success: false,
      error: errorData,
    })

    throw error
  }

  run(id: string, block: () => unknown) {
    new Promise((resolve) => {
      resolve(block())
    })
      .then((result: unknown) => this.resolveSuccess(id, result))
      .catch((error: unknown) => this.resolveError(id, error))
  }

  handleEvent(event: JavaScriptRunEvent) {
    this.run(event.id, () => {
      const globals = createScriptingGlobals({
        client: this.delegate.component.view.page.parent,
        page: this.delegate.component.view.page,
        view: this.delegate.component.view,
        component: this.delegate.component,
      })

      const f = toUserScript(event.function, this.ref, globals)
      return event.args !== undefined ? f.runNamed(event.args) : f()
    })
  }
}
