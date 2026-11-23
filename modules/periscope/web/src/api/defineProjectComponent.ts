import { useElement } from './useElement'
import type {
  ComponentDefinition,
  ComponentRender,
  ElementProps,
  EventSchema,
  ProjectComponent,
  ProjectComponentEvent,
  PropSchema,
} from './periscopeApi'
import { defaultsOf, propsSchema, STYLE_SCHEMA } from './schema'

function eventsOf(events: EventSchema): ProjectComponentEvent[] {
  return Object.entries(events).map(([name, event]) => ({
    name,
    description: event.description,
    schema: event.schema,
  }))
}

function positionOf(position: PropSchema | undefined) {
  if (position == null) {
    return {}
  }

  const schema = propsSchema(position)
  return { position: { schema, defaults: defaultsOf(schema.properties) } }
}

export function defineProjectComponent(
  definition: ComponentDefinition
): ProjectComponent {
  const { render, props = {}, events = {}, position, ...metadata } = definition

  const schema = propsSchema(props)
  const clashes = Object.keys(events).filter((name) => name in props)

  const projectComponent = ((given: ElementProps) =>
    useElement(projectComponent, given)) as ProjectComponent

  return Object.assign(projectComponent, {
    render: render as ComponentRender<never>,
    periscope: {
      ...metadata,
      schema,
      defaults: defaultsOf(schema.properties),
      ...positionOf(position),
      events: eventsOf(events as EventSchema),
      diagnostics: clashes.map(
        (name) => `'${name}' is both a prop and an event.`
      ),
    },
  })
}

export function describeComponent({ periscope }: ProjectComponent) {
  const defaults = { ...periscope.defaults, style: STYLE_SCHEMA.default }
  return {
    description: periscope.description,
    icon: periscope.icon,
    defaultSize: periscope.defaultSize,
    source: periscope.source,
    events: periscope.events,
    diagnostics: periscope.diagnostics,
    position: periscope.position && {
      ...periscope.position.schema,
      default: periscope.position.defaults,
    },
    variants: (periscope.variants ?? []).map((variant) => ({
      name: variant.name,
      description: variant.description,
      defaultSize: variant.defaultSize,
      props: { ...defaults, ...variant.props },
    })),
    defaults,
    schema: {
      ...periscope.schema,
      default: defaults,
      properties: { ...periscope.schema.properties, style: STYLE_SCHEMA },
    },
  }
}
