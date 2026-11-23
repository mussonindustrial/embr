import { ClientStore } from '@inductiveautomation/perspective-client'
import { describeComponent, ProjectComponent, Values } from '@/api'
import { waitForClientResourceStore } from '@/util'

type Payload = Parameters<ClientStore['connection']['send']>[1]

type ComponentDefaults = Record<
  string,
  {
    defaults: Values
    positionDefaults?: Values
  }
>

const PROTOCOL = {
  DEFAULTS: 'periscope-component-defaults',
}

const HELD_UNTIL_LOADED = new Set(['view-start', 'view-restart'])

function componentDefaultsOf(
  components: Map<string, ProjectComponent>
): ComponentDefaults {
  return Object.fromEntries(
    [...components].map(([id, component]) => {
      const { defaults, position } = describeComponent(component)

      if (position == null) {
        return [id, { defaults }]
      }

      return [id, { defaults, positionDefaults: position.default }]
    })
  )
}

async function loadComponentDefaults(): Promise<ComponentDefaults | null> {
  const store = await waitForClientResourceStore()
  const components = await store.getComponents()

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
      send(PROTOCOL.DEFAULTS, defaults)
    }

    send(protocol, payload)
  }

  const releaseHeldMessages = (loaded: ComponentDefaults | null) => {
    defaults = loaded

    heldMessages
      .splice(0)
      .forEach(([protocol, payload]) => forward(protocol, payload))
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
      console.warn(
        'Cannot send component defaults, components did not load.',
        error
      )
      return null
    })
    .then(releaseHeldMessages)
}
