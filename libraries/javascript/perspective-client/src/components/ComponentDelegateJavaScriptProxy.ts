import {
  ComponentStoreDelegate,
  JsObject,
} from '@inductiveautomation/perspective-client'
import { toUserScript } from '@embr-js/utils'
import _ from 'lodash'
import { createScriptingGlobals } from '../scripting/ScriptingGlobals'

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

export class ComponentDelegateJavaScriptProxy {
  private readonly delegate: ComponentStoreDelegate
  private readonly props?: JsObject

  constructor(delegate: ComponentStoreDelegate, props?: JsObject) {
    this.delegate = delegate
    this.props = props
  }

  handles(eventName: string) {
    return eventName == 'js-run'
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
      const property = _.get(this.props, event.property)
      const globals = createScriptingGlobals({
        client: this.delegate.component.view.page.parent,
        page: this.delegate.component.view.page,
        view: this.delegate.component.view,
        component: this.delegate.component,
      })

      const f = toUserScript(event.function, property, globals)
      return event.args !== undefined ? f.runNamed(event.args) : f()
    })
  }
}
