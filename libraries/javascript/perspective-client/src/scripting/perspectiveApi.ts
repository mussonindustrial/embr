import type { ClientStore } from '@inductiveautomation/perspective-client'

import type { CallingContext } from '../stores/ScriptingStore'
import { createViewFunction } from './createView'
import { makeSendMessage } from './sendMessage'

export type PerspectiveApiContext = CallingContext & { client: ClientStore }

export function createPerspectiveApi(context: PerspectiveApiContext) {
  return {
    /** The Perspective stores the script runs in. */
    context,
    /** Sends a message to message handlers on the Gateway. */
    sendMessage: makeSendMessage(context),
    /** Renders a Perspective view, for use in a React component. */
    createView: createViewFunction(context),
  }
}
