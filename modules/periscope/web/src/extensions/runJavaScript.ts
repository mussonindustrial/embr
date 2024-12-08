import { toFunction } from '@embr-js/utils'
import { ClientStore } from '@inductiveautomation/perspective-client'

export const PROTOCOL = {
  RUN: 'periscope-js-run',
  RESOLVE: 'periscope-js-resolve',
  ERROR: 'periscope-js-error',
}

export function installRunJavaScript(clientStore: ClientStore) {
  const globals = {
    context: {
      client: clientStore,
    },
  }

  clientStore.connection.handlers.set(PROTOCOL.RUN, (payload) => {
    const { function: functionLiteral, args, id } = payload

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
      const f = toFunction(functionLiteral, globals)
      const result = args !== undefined ? f(args) : f()
      resolve(result)
    })
      .then((result: unknown) => resolveSuccess(result))
      .catch((error: unknown) => resolveError(error))
  })
}
