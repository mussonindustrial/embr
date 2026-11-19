import { ClientStore } from '@inductiveautomation/perspective-client'
import { describeComponent, LibraryComponent } from '@/api/periscopeApi'
import { waitForClientResourceStore } from '@/util'

type Payload = Parameters<ClientStore['connection']['send']>[1]

type ComponentDefaults = Record<
  string,
  {
    defaults: Record<string, unknown>
    positionDefaults?: Record<string, unknown>
  }
>

const PROTOCOL = 'embr-periscope-component-defaults'

const HELD_UNTIL_LOADED = new Set(['view-start', 'view-restart'])

function componentDefaultsOf(
  components: Map<string, LibraryComponent>
): ComponentDefaults {
  const result: ComponentDefaults = {}

  for (const [key, component] of components) {
    const { defaults, position } = describeComponent(component)

    if (position == null) {
      result[key] = { defaults }
    } else {
      result[key] = { defaults, positionDefaults: position.default }
    }
  }

  return result
}

async function loadComponentDefaults(): Promise<ComponentDefaults | null> {
  const store = await waitForClientResourceStore()
  const components = await store.libraryComponents()

  if (components.size === 0) {
    return null
  }

  return componentDefaultsOf(components)
}

export function installComponentDefaults(clientStore: ClientStore) {
  const connection = clientStore.connection
  const send = connection.send.bind(connection)
  const heldMessages: [string, Payload][] = []
  let defaults: ComponentDefaults | null | undefined
  let sentOn: WebSocket | null = null

  const forward = (protocol: string, payload: Payload) => {
    if (defaults != null && connection.webSocket !== sentOn) {
      sentOn = connection.webSocket
      send(PROTOCOL, defaults)
    }

    send(protocol, payload)
  }

  const releaseHeldMessages = (loaded: ComponentDefaults | null) => {
    defaults = loaded

    for (const [protocol, payload] of heldMessages.splice(0)) {
      forward(protocol, payload)
    }
  }

  connection.send = (protocol: string, payload: Payload) => {
    if (defaults !== undefined) {
      forward(protocol, payload)
    } else if (HELD_UNTIL_LOADED.has(protocol)) {
      heldMessages.push([protocol, payload])
    } else {
      send(protocol, payload)
    }
  }

  loadComponentDefaults()
    .catch((error: unknown) => {
      console.error('Error loading component defaults', error)
      return null
    })
    .then(releaseHeldMessages)
}
