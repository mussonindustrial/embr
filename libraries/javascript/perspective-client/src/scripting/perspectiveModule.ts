import type { ClientStore } from '@inductiveautomation/perspective-client'

import type { CallingContext } from '../stores/ScriptingStore'
import { createViewFunction } from './createView'
import { makeSendMessage } from './sendMessage'

export type ModuleContext = CallingContext & { client: ClientStore }

export function createPerspectiveModule(context: ModuleContext) {
  return {
    /** The Perspective stores the script runs in. */
    context,
    /** Sends a message to message handlers on the Gateway. */
    sendMessage: makeSendMessage(context),
    /** Renders a Perspective view, for use in a React component. */
    createView: createViewFunction(context),
  }
}
