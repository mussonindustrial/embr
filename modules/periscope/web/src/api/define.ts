import { renderElement } from './element'
import type {
  ComponentDefinition,
  ComponentRender,
  ElementProps,
  EventSchema,
  LibraryComponent,
  LibraryComponentEvent,
  PropSchema,
} from './periscopeApi'
import { defaultsOf, propsSchema } from './schema'

function eventsOf(events: EventSchema): LibraryComponentEvent[] {
  const libraryEvents: LibraryComponentEvent[] = []

  for (const [name, event] of Object.entries(events)) {
    libraryEvents.push({
      name,
      description: event.description,
      schema: event.schema,
    })
  }

  return libraryEvents
}

function positionOf(position: PropSchema | undefined) {
  if (position == null) {
    return {}
  }

  const schema = propsSchema(position)
  return { position: { schema, defaults: defaultsOf(schema.properties) } }
}

export function defineLibraryComponent(
  definition: ComponentDefinition
): LibraryComponent {
  const { render, props = {}, events = {}, position, ...metadata } = definition

  const schema = propsSchema(props)
  const clashes = Object.keys(events).filter((name) => name in props)

  const libraryComponent = ((given: ElementProps) =>
    renderElement(libraryComponent, given)) as LibraryComponent
  libraryComponent.displayName = metadata.name ?? metadata.id

  return Object.assign(libraryComponent, {
    render: render as ComponentRender<never>,
    periscope: {
      ...metadata,
      schema,
      defaults: defaultsOf(schema.properties),
      ...positionOf(position),
      events: eventsOf(events as EventSchema),
      diagnostics: clashes.map(
        (name) => `'${name}' is both a prop and an event`
      ),
    },
  })
}
