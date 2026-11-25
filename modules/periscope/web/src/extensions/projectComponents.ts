import {
  ComponentMeta,
  ComponentRegistry,
} from '@inductiveautomation/perspective-client'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'

import { COMPONENT_TYPE_PREFIX, toComponentType } from '@/api'
import {
  ANIMATIONS_PAUSED,
  DROP_ACTIVE,
  PLACEHOLDER,
  ProjectComponentDesignDelegate,
  ProjectComponentMeta,
} from '@/components/embedding/project'
import { waitForClientResourceStore } from '@/util'

const STYLES = `
[${PLACEHOLDER}] {
  flex: 1 1 auto;
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 2rem;
  padding: 0.25rem 0.5rem;
  border: 1px dashed currentColor;
  border-radius: 4px;
  font-size: 0.75rem;
  opacity: 0.55;
  pointer-events: none;
}
[${DROP_ACTIVE}] {
  outline: 2px solid var(--callToAction, #2e9cdb);
  outline-offset: -2px;
}
[${ANIMATIONS_PAUSED}],
[${ANIMATIONS_PAUSED}] *,
[${ANIMATIONS_PAUSED}] *::before,
[${ANIMATIONS_PAUSED}] *::after {
  animation-play-state: paused !important;
}
`

export function installProjectComponents() {
  const metas = new Map<string, ComponentMeta>()
  const getRegisteredMeta = ComponentRegistry.getMeta

  const getMeta = (type: string) => {
    const registered = getRegisteredMeta.call(ComponentRegistry, type)

    if (registered != null || !type?.startsWith(COMPONENT_TYPE_PREFIX)) {
      return registered
    }

    if (!metas.has(type)) {
      metas.set(type, new ProjectComponentMeta(type))
    }

    return metas.get(type)
  }

  Object.defineProperty(ComponentRegistry, 'getMeta', {
    value: getMeta,
    configurable: true,
    writable: true,
  })
}

export async function installProjectComponentDesign() {
  const styles = document.createElement('style')
  styles.textContent = STYLES
  document.head.appendChild(styles)

  const store = await waitForClientResourceStore()
  const components = await store.getComponents()

  components.forEach((component, id) => {
    if (component.periscope.position != null) {
      InteractionRegistry.registerInteractionDelegates(
        new ProjectComponentDesignDelegate(toComponentType(id))
      )
    }
  })
}
