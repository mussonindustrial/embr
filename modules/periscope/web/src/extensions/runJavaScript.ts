import { getChildStore } from '@embr-js/perspective-client'
import { toUserScript, UserScriptParams } from '@embr-js/utils'
import { ClientStore } from '@inductiveautomation/perspective-client'

export const PROTOCOL = {
  RUN: 'periscope-js-run',
  RESOLVE: 'periscope-js-resolve',
  ERROR: 'periscope-js-error',
}

type RunJavaScriptPayload = {
  function: string
  args: UserScriptParams
  id: string
  context: RunJavaScriptContext
}

type RunJavaScriptContext = {
  view?: {
    id: string
    mountPath: string
    resourcePath: string
  }
  component?: {
    componentAddressPath: string
  }
}

function getChildPath(componentAddressPath?: string) {
  return componentAddressPath?.split(':').map(Number) ?? []
}

export function installRunJavaScript(clientStore: ClientStore) {
  const thisArg = clientStore

  clientStore.connection.handlers.set(PROTOCOL.RUN, (payload) => {
    const {
      function: functionLiteral,
      args,
      id,
      context,
    } = payload as RunJavaScriptPayload

    function resolveSuccess(data: unknown) {
      clientStore.connection.send(PROTOCOL.RESOLVE, {
        id,
        success: true,
        data,
      })
    }

    function resolveError(error: unknown) {
      const errorData =
        error instanceof Error
          ? {
              name: error.name,
              message: error.message,
              stack: error.stack,
            }
          : error

      clientStore.connection.send(PROTOCOL.ERROR, {
        id,
        success: false,
        error: errorData,
      })

      throw error
    }

    new Promise((resolve) => {
      const view = clientStore.page.findView(
        context.view?.resourcePath ?? '',
        context.view?.mountPath ?? ''
      )

      const componentPath = getChildPath(
        context.component?.componentAddressPath
      )
      const component = getChildStore(view, componentPath)

      const globals = Embr.scripting.createGlobals({
        client: clientStore,
        page: clientStore.page,
        view,
        component,
      })

      const f = toUserScript(functionLiteral, thisArg, globals)
      resolve(f.runNamed(args))
    })
      .then((result: unknown) => resolveSuccess(result))
      .catch((error: unknown) => resolveError(error))
  })
}
