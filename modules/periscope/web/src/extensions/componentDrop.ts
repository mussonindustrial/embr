import { ClientStore } from '@inductiveautomation/perspective-client'

import type { Values } from '@/api'

type ComponentDrop = {
  type: string
  payload: {
    type: string
    name: string
    props: Values
    width?: number
    height?: number
  }
}

type ComponentManager = {
  addComponents(
    components: object[],
    position: { x: number; y: number; width?: number; height?: number }
  ): void
}

const DROP_TYPE = 'embr-periscope-project-component'

function parseComponentDrop(event: DragEvent): ComponentDrop | null {
  try {
    const data = event.dataTransfer?.getData('text/plain') ?? ''
    const drop = JSON.parse(data)

    return drop?.type === DROP_TYPE ? drop : null
  } catch {
    return null
  }
}

export function installComponentDrop(clientStore: ClientStore) {
  document.addEventListener(
    'drop',
    (event) => {
      const drop = parseComponentDrop(event)

      if (drop == null) {
        return
      }

      event.preventDefault()
      event.stopPropagation()

      const { type, name, props, width, height } = drop.payload
      const page: { componentManager: ComponentManager } =
        clientStore.page as never

      page.componentManager.addComponents(
        [
          {
            type,
            props,
            meta: { name },
            position: {},
            version: -1,
            events: {},
          },
        ],
        { x: event.clientX, y: event.clientY, width, height }
      )
    },
    true
  )
}
