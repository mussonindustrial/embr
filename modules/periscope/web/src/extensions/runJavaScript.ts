import { toFunction } from '@embr-js/utils'
import { ClientStore } from '@inductiveautomation/perspective-client'

const PROTOCOL = 'periscope-runJavaScript'
const PROTOCOL_RESULT = 'periscope-runJavaScript-result'

export function installRunJavaScript(clientStore: ClientStore) {
  const globals = { client: clientStore }

  clientStore.connection.handlers.set(PROTOCOL, (payload) => {
    const { function: functionLiteral, args, id } = payload
    const f = toFunction(functionLiteral, globals)

    let responsePayload

    try {
      const value = args !== undefined ? f(args) : f()
      responsePayload = { id, result: { success: true, value } }
    } catch (error) {
      const value =
        typeof error === 'string'
          ? error.toUpperCase()
          : error instanceof Error
            ? error.message
            : error
      responsePayload = { id, result: { success: false, value } }
      clientStore.connection.send(PROTOCOL_RESULT, responsePayload)
      throw error
    }

    clientStore.connection.send(PROTOCOL_RESULT, responsePayload)
  })
}
