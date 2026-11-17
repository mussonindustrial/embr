import { getDesignerStore } from '@embr-js/perspective-client'

type ComponentDrop = {
  type: string
  payload: {
    type: string
    name: string
    props: Record<string, unknown>
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

const DROP_TYPE = 'embr-periscope-library-component'

function parseComponentDrop(event: DragEvent): ComponentDrop | undefined {
  try {
    const data = event.dataTransfer?.getData('text/plain') ?? ''
    const drop = JSON.parse(data)

    return drop?.type === DROP_TYPE ? drop : undefined
  } catch {
    return undefined
  }
}

export function installComponentDrop() {
  const store = getDesignerStore()

  if (store == null) {
    return
  }

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
      const page = store.page as unknown as {
        componentManager: ComponentManager
      }

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
