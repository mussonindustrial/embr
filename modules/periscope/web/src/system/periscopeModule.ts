import type { ESModule } from '@embr-js/utils/types'
import { toast } from 'react-toastify'

type Resources = {
  resource(path: string): Promise<ESModule>
}

export function createPeriscopeModule(resources: Resources) {
  return {
    /** Shows a toast notification in the Perspective session. */
    toast,
    /** Loads another client resource by path, resolving to its module exports. */
    resource: (path: string) => resources.resource(path),
  }
}
