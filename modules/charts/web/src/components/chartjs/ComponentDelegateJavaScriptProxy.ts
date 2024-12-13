import {
  ComponentStoreDelegate,
  JsObject,
} from '@inductiveautomation/perspective-client'
import { readCSSVar, toUserScript } from '@embr-js/utils'
import _ from 'lodash'

const EVENTS = {
  JS_ERROR: 'js-error',
  JS_RESOLVE: 'js-resolve',
  JS_RUN: 'js-run',
}

export type JavaScriptRunEvent = {
  id: string
  property: string
  function: string
  args: any
}

export class ComponentDelegateJavaScriptProxy {
  private readonly delegate: ComponentStoreDelegate
  private readonly props?: JsObject
  private readonly thisArg: JsObject

  constructor(delegate: ComponentStoreDelegate, props?: JsObject) {
    this.delegate = delegate
    this.props = props
    this.thisArg = {
      component: delegate.component,
      view: delegate.component.view,
      page: delegate.component.view.page,
      client: delegate.component.view.page.parent,
      util: {
        readCSSVar: readCSSVar.bind(this, props?.element),
        readProperty: (property: string) =>
          delegate.component.interpolate(property).$v,
      },
    }
  }

  handles(eventName: string) {
    return eventName == 'js-run'
  }

  private resolveSuccess(id: string, data: unknown) {
    this.delegate.fireEvent(EVENTS.JS_RESOLVE, {
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
      const property = _.get(this.props, event.property)

      const f = toUserScript(event.function, { ...this.thisArg, property })
      return event.args !== undefined ? f.runNamed(event.args) : f()
    })
  }
}
