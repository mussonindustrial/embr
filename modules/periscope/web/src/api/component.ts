import type { ProjectComponent } from './periscopeApi'

export const COMPONENT_TYPE_PREFIX = 'embr.periscope.component.'

function isProjectComponent(value: unknown): value is ProjectComponent {
  const component = value as Partial<ProjectComponent> | null
  return (
    typeof value === 'function' &&
    typeof component?.render === 'function' &&
    typeof component.periscope?.schema === 'object'
  )
}

export function toComponentType(id: string) {
  return COMPONENT_TYPE_PREFIX + id
}

export function toComponentId(type: string) {
  return type.slice(COMPONENT_TYPE_PREFIX.length)
}

export function readComponent(exports: unknown): ProjectComponent {
  const component = (exports as { default?: unknown } | null)?.default

  if (!isProjectComponent(component)) {
    throw new Error('The default export must be made with defineComponent.')
  }

  return component
}
