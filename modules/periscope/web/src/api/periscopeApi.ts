import type { ESModule } from '@embr-js/utils/types'
import type {
  ComponentProps,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import type {
  ComponentPropsWithoutRef,
  CSSProperties,
  ElementType,
  HTMLAttributes,
  ReactElement,
  ReactNode,
  Ref,
} from 'react'
import { toast } from 'react-toastify'
import { useComponent } from './context'
import { defineLibraryComponent } from './define'
import type { PropSetter } from './props'
import {
  createEvent,
  createProp,
  defaultsOf,
  type JsonSchema,
  objectSchema,
} from './schema'

export type { PropSetter } from './props'

export const COMPONENT_TYPE_PREFIX = 'embr.periscope.component-library.'

export function keyOf(namespace: string, id: string) {
  return `${namespace}.${id}`
}

/**
 * A prop of a library component: its type, and how the Designer edits it.
 *
 * A component always receives a prop that has a `default()`. Any other prop
 * may be missing from a view, so the component receives it as optional.
 * Inside an `object`, `array` or event, a field is required unless it is
 * `optional()` or has a `default()`.
 */
export interface Prop<T> {
  readonly schema: JsonSchema
  readonly isOptional: boolean
  readonly hasDefault: boolean
  /** Never set. Carries the prop's type. */
  readonly type?: T
  /** Shown in the Designer's property editor. */
  describe(description: string): this
  /** The value a new component starts with. */
  default(value: T): this & Defaulted
  /** The prop can be left without a value. */
  optional(): Prop<T | undefined>
  /** The prop can be `null`. */
  nullable(): Prop<T | null> & Pick<this, 'hasDefault'>
}

type Defaulted = { readonly hasDefault: true }

export interface NumberProp<T extends number | null | undefined = number>
  extends Prop<T> {
  optional(): NumberProp<T | undefined>
  nullable(): NumberProp<T | null> & Pick<this, 'hasDefault'>
  min(value: number): this
  max(value: number): this
  /** The value must be a multiple of the step. */
  step(value: number): this
  /** Only whole numbers are valid. */
  int(): this
}

/** The props of a library component, by name. */
export type PropSchema = Record<string, Prop<unknown>>

/** The type of a prop. */
export type Infer<P> = P extends Prop<infer T> ? T : never

type PropValue<P> = P extends Defaulted ? Infer<P> : Infer<P> | undefined

type Simplify<T> = { [K in keyof T]: T[K] } & {}

type RequiredKeys<S, Required> = {
  [K in keyof S]: undefined extends Infer<S[K]>
    ? never
    : S[K] extends Required
      ? K
      : never
}[keyof S]

type InferFields<S, Required> = Simplify<
  { [K in RequiredKeys<S, Required>]: Infer<S[K]> } & {
    [K in Exclude<keyof S, RequiredKeys<S, Required>>]?: Infer<S[K]>
  }
>

/** The props a component receives for a prop schema. */
export type InferProps<S extends PropSchema> = InferFields<S, Defaulted>

type InferShape<S extends PropSchema> = InferFields<S, unknown>

/** An event of a library component, which the Designer can attach scripts to. */
export interface ComponentEvent<T> {
  readonly schema: JsonSchema
  readonly description?: string
  /** Never set. Carries the type of the event object. */
  readonly type?: T
  /** Shown beside the event in the Designer. */
  describe(description: string): ComponentEvent<T>
}

/** The events of a library component, by name. */
export type EventSchema = Record<string, ComponentEvent<unknown>>

/** The functions a component receives to fire its events. */
export type InferEvents<E extends EventSchema> = {
  [K in keyof E]: E[K] extends ComponentEvent<infer T>
    ? keyof T extends never
      ? () => void
      : (event: T) => void
    : never
}

const STYLE = 'urn:ignition-schema:schemas/style-properties.schema.json'
const STYLE_SCHEMA = { $ref: STYLE, default: { classes: '' } }

type Literal = string | number | boolean | null

/**
 * Describes the props of a library component.
 *
 * Each prop is built by chaining, such as
 * `prop.number().min(0).default(10).describe('The row count.')`.
 */
export const prop = {
  string: () => createProp<Prop<string>>({ type: 'string' }),
  number: () => createProp<NumberProp>({ type: 'number' }),
  boolean: () => createProp<Prop<boolean>>({ type: 'boolean' }),
  /** A date and time, which the component receives as a `Date`. */
  date: () => createProp<Prop<Date>>({ type: 'date' }),
  /** One of a fixed set of values, edited with a dropdown. */
  enum: <const V extends readonly (string | number)[]>(values: V) =>
    createProp<Prop<V[number]>>({
      type: typeof values[0] === 'number' ? 'number' : 'string',
      enum: values,
    }),
  /** Exactly one value. Mostly used inside a `union`. */
  literal: <const V extends Literal>(value: V) =>
    createProp<Prop<V>>(
      value === null ? { type: 'null' } : { type: typeof value, enum: [value] }
    ),
  /** A CSS color, edited with the color picker. */
  color: () => createProp<Prop<string>>({ type: 'string', format: 'color' }),
  /** An icon path such as `material/home`, edited with the icon picker. */
  icon: () => createProp<Prop<string>>({ type: 'string', format: 'icon-path' }),
  /** The path of a Perspective view, edited with the view picker. */
  viewPath: () =>
    createProp<Prop<string>>({ type: 'string', format: 'view-path' }),
  /** Perspective style classes, edited with the style class picker. */
  styleClasses: () =>
    createProp<Prop<string>>({ type: 'string', format: 'style-list' }),
  /** A Perspective style object, edited with the style editor. */
  style: () =>
    createProp<Prop<StyleObject> & Defaulted>({
      $ref: STYLE,
      default: { classes: '' },
    }),
  /**
   * Any JSON value, such as rows from a query, optionally described by a JSON
   * schema of your own. Its type is only checked in code.
   */
  custom: <T = unknown>(schema: Record<string, unknown> = {}) =>
    createProp<Prop<T>>(schema),
  /** A list whose items are all described by one prop. */
  array: <P extends Prop<unknown>>(items: P) =>
    createProp<Prop<Infer<P>[]>>({ type: 'array', items: items.schema }),
  /** An object with a fixed set of props. It defaults to their defaults. */
  object: <S extends PropSchema>(properties: S) => {
    const schema = objectSchema(properties)
    return createProp<Prop<InferShape<S>> & Defaulted>({
      ...schema,
      default: defaultsOf(schema.properties),
    })
  },
  /** A value that matches any one of the given props. */
  union: <const P extends readonly Prop<unknown>[]>(options: P) =>
    createProp<Prop<Infer<P[number]>>>({
      anyOf: options.map((option) => option.schema),
    }),
}

/**
 * Describes an event of a library component.
 *
 * The component receives a function of the same name, and calling it runs the
 * scripts the Designer attached to the event, with the object it is given.
 */
export function event(): ComponentEvent<Record<never, never>>
export function event<S extends PropSchema>(
  payload: S
): ComponentEvent<InferShape<S>>
export function event(payload: PropSchema = {}): ComponentEvent<unknown> {
  return createEvent(objectSchema(payload))
}

/**
 * The props of `Root`: those of the element it renders, `div` unless `as`
 * names another.
 */
export type RootProps<E extends ElementType = 'div'> = {
  /** The element or component to render. Defaults to a `div`. */
  as?: E
  /**
   * Renders the only child instead, with the root's attributes merged onto
   * it, such as a `ChildSlot` that is the component's outermost element.
   */
  asChild?: boolean
  ref?: Ref<HTMLElement> | Ref<SVGElement>
} & Omit<ComponentPropsWithoutRef<E>, 'as' | 'asChild'>

export type ChildRenderOptions = {
  className?: string
  style?: CSSProperties
}

/** A child the Designer is dropping, moving or resizing in a child slot. */
export type PositionChange = {
  /**
   * Where the child is in the child slot and how big it is: the corner
   * nearest the slot's top left, and its size, in the pixels of the slot.
   */
  bounds: { x: number; y: number; width: number; height: number }
  /** The size of the child slot. */
  slot: { width: number; height: number }
  /** Why the position is changing. */
  reason: 'drop' | 'move' | 'resize'
}

/** A handle that resizes a child in the Designer, by the side or corner it is on. */
export type ResizeHandle = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw'

/** Where a library component is shown. */
export type ComponentMode = 'session' | 'design' | 'preview'

/** A ready-made configuration of a component, shown in the component palette. */
export type ComponentVariant<P = Record<string, unknown>> = {
  /** The name shown in the component palette. */
  name: string
  /** Shown as the variant's tooltip in the component palette. */
  description?: string
  /** The props a new component starts with, over the defaults. */
  props: Partial<P>
  /** The size a new component starts with, where its container uses one. */
  defaultSize?: { width: number; height: number }
}

/** A component placed inside a library component in the Designer. */
export type Child<C extends PropSchema = PropSchema> = {
  /** Stable for the child, for use as its React key. */
  key: string
  /** The name of the child in the Designer. */
  name: string
  /** The Perspective component type of the child. */
  type: string
  /** Where the child is among the children, in the order of the Designer. */
  index: number
  /** The values of the container's `position` props, set on the child. */
  position: InferProps<C>
  /**
   * Whether the child is selected in the Designer, or holds the selection.
   * Always false in a session. Use it to show a child that would be hidden.
   */
  selected: boolean
  /** Renders the child. The class and style apply to its outermost element. */
  render(options?: ChildRenderOptions): ReactNode
}

export type ChildSlotProps<C extends PropSchema = PropSchema> = Omit<
  HTMLAttributes<HTMLElement>,
  'children'
> & {
  /** The element the slot renders. Defaults to a `div`. */
  as?: ElementType
  /**
   * The position props of the slot. It renders the children that have these
   * values, and a child dropped on it in the Designer is given them.
   */
  where?: Partial<InferProps<C>>
  /**
   * Chooses the position props of a child from the bounds it is dropped, moved
   * or resized to in the Designer, such as its coordinates or the cell of a
   * grid. The result is set over the `where` of the slot.
   */
  getPosition?: (change: PositionChange) => Partial<InferProps<C>>
  /**
   * Shows handles that resize a child in the Designer, which calls `getPosition`
   * with the new bounds. `true` shows all eight.
   */
  resize?: boolean | ResizeHandle[]
  /** Shown in the Designer while the slot has no children. */
  placeholder?: ReactNode
  /** Renders each child, to style or wrap it. Defaults to `child.render()`. */
  children?: (child: Child<C>) => ReactNode
}

/** What a library component can use besides its props. */
export type ComponentContext<
  S extends PropSchema = PropSchema,
  C extends PropSchema = PropSchema,
> = {
  /**
   * The components placed inside this one, in order. Low-level: a `ChildSlot`
   * renders them for you. Empty unless the component declares `position`.
   */
  readonly children: Child<C>[]
  /**
   * An element that renders the children matching its `where`, in order. In
   * the Designer it is also where those children are dropped.
   */
  ChildSlot: (props: ChildSlotProps<C>) => ReactElement | null
  /**
   * The component's outermost element, which every component renders once.
   * It applies Perspective's position, style, classes and DOM events, and
   * merges its own class, style, ref and event handlers with them.
   */
  Root: <E extends ElementType = 'div'>(
    props: RootProps<E>
  ) => ReactElement | null
  /** Reads a prop of the component and returns a function that writes it. */
  useProp<K extends keyof S & string>(
    name: K
  ): [PropValue<S[K]>, PropSetter<Infer<S[K]>>]
  useProp<T = unknown>(path: string): [T, PropSetter<T>]
  /**
   * Where the component is shown: in a session, in the Designer while it is
   * being designed, or in the Designer's preview.
   */
  readonly mode: ComponentMode
  /** The Perspective props of the component, as they are now. */
  readonly perspective: ComponentProps<Record<string, unknown>>
}

/** Renders a library component from its props. */
export type ComponentRender<
  P,
  S extends PropSchema = PropSchema,
  C extends PropSchema = PropSchema,
> = (props: P, context: ComponentContext<S, C>) => ReactNode

/** What a library component declares about itself. */
export type ComponentMetadata = {
  /** The stable, project-unique id that views store as the component type. */
  id: string
  /** The name shown in the Designer. Defaults to the id. */
  name?: string
  /** Shown as the component's tooltip in the component palette. */
  description?: string
  /** The icon shown in the Designer, as `library/name`. */
  icon?: string
  /** The folder the component is shown under in the component palette. */
  category?: string
  /** The size a new component starts with, where its container uses one. */
  defaultSize?: { width: number; height: number }
}

export type ComponentDefinition<
  S extends PropSchema = PropSchema,
  E extends EventSchema = EventSchema,
  C extends PropSchema = PropSchema,
> = ComponentMetadata & {
  /** The props of the component, which the Designer's property editor shows. */
  props?: S
  /**
   * The events of the component, which the Designer attaches scripts to.
   * An event cannot have the name of a prop.
   */
  events?: E & { [K in keyof S & keyof E]: never }
  /**
   * Makes the component a container. The props are set on each child's
   * position, where the Designer's property editor shows them.
   */
  position?: C
  /** Ready-made configurations, shown under the component in the palette. */
  variants?: ComponentVariant<NoInfer<InferProps<S>>>[]
  /** Renders the component from its props. */
  render: ComponentRender<InferProps<S> & InferEvents<E>, S, C>
}

export type LibraryComponentEvent = {
  name: string
  description?: string
  schema: JsonSchema
}

/** A child given to a library component that is rendered as a React element. */
export type PlacedChild<C = Record<string, unknown>> = {
  /** The position props it is placed with, over their defaults. */
  position: Partial<C>
  node: ReactNode
  key?: string
}

/** The props of a library component that is rendered as a React element. */
export type ElementProps<
  P = Record<string, unknown>,
  C = Record<string, unknown>,
> = Partial<P> & {
  /** Added to the classes of the component's outermost element. */
  className?: string
  /** Set over the style of the component's outermost element. */
  style?: CSSProperties
  /** Shown in the slot of a container that matches its default position. */
  children?: ReactNode
  /** Children of a container that are placed with a position of their own. */
  placed?: PlacedChild<C>[]
}

type ElementFunction<P, C> = {
  bivariant(props: ElementProps<P, C>): ReactElement | null
}['bivariant']

/**
 * A component made with `defineComponent`. A component library lists it so
 * views can use it, and other components render it as a React element.
 */
export type LibraryComponent<
  P = Record<string, unknown>,
  C = Record<string, unknown>,
> = ElementFunction<P, C> & {
  displayName?: string
  render: ComponentRender<never>
  periscope: ComponentMetadata & {
    schema: JsonSchema & { properties: Record<string, JsonSchema> }
    defaults: Record<string, unknown>
    events: LibraryComponentEvent[]
    variants?: ComponentVariant[]
    /** The position props of a container. Absent when it has no children. */
    position?: {
      schema: JsonSchema & { properties: Record<string, JsonSchema> }
      defaults: Record<string, unknown>
    }
    /** The client resource that defined the component, when it is known. */
    source?: string
    /** What is wrong with the definition, shown in the component palette. */
    diagnostics: string[]
  }
}

export function describeComponent({ periscope }: LibraryComponent) {
  const defaults = { ...periscope.defaults, style: STYLE_SCHEMA.default }
  return {
    id: periscope.id,
    name: periscope.name ?? periscope.id,
    description: periscope.description,
    icon: periscope.icon,
    category: periscope.category,
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

type DefineComponent = <
  S extends PropSchema = Record<never, never>,
  E extends EventSchema = Record<never, never>,
  C extends PropSchema = Record<never, never>,
>(
  definition: ComponentDefinition<S, E, C>
) => LibraryComponent<InferProps<S> & InferEvents<E>, InferProps<C>>

type Resources = {
  resource(path: string): Promise<ESModule>
  source?(): string | undefined
}

const LIBRARY_EXPORT =
  'The default export must be made with defineComponentLibrary'

const LIBRARY_COMPONENTS =
  'Components must be a list of components made with defineComponent'

const LIBRARY_NAMESPACE =
  'The namespace must start with a letter and contain only letters, digits, - and _'

const NAMESPACE = /^[A-Za-z][\w-]*$/

const LIBRARY = Symbol.for('embr.periscope.component-library')

function isLibraryComponent(value: unknown): value is LibraryComponent {
  const component = value as Partial<LibraryComponent> | null
  return (
    (typeof component === 'object' || typeof component === 'function') &&
    typeof component?.render === 'function' &&
    typeof component.periscope?.id === 'string'
  )
}

/** A component library made with `defineComponentLibrary`. */
export type ComponentLibrary = {
  /** Qualifies the ids of the library's components, such as `acme.button`. */
  readonly namespace: string
  /** The components that views can use. */
  readonly components: readonly LibraryComponent[]
}

/**
 * Defines a component library. It is the default export of a Component Library
 * resource, and lists the components views can use under its namespace.
 */
export function defineComponentLibrary(library: {
  namespace: string
  components: readonly LibraryComponent[]
}): ComponentLibrary {
  const { namespace, components } = library ?? {}

  if (typeof namespace !== 'string' || !NAMESPACE.test(namespace)) {
    throw new Error(LIBRARY_NAMESPACE)
  }

  if (!Array.isArray(components) || !components.every(isLibraryComponent)) {
    throw new Error(LIBRARY_COMPONENTS)
  }

  return Object.freeze({
    namespace,
    components: Object.freeze([...components]),
    [LIBRARY]: true,
  })
}

export function libraryOf(exports: unknown): ComponentLibrary {
  const library = (exports as { default?: unknown } | null)?.default as
    | (ComponentLibrary & { [LIBRARY]?: true })
    | null
    | undefined

  if (library == null || library[LIBRARY] !== true) {
    throw new Error(LIBRARY_EXPORT)
  }

  return library
}

export function createPeriscopeApi(resources: Resources) {
  const defineComponent = ((definition: ComponentDefinition) => {
    const component = defineLibraryComponent(definition)
    component.periscope.source = resources.source?.()

    return component
  }) as unknown as DefineComponent

  return {
    /** Shows a toast notification in the Perspective session. */
    toast,
    /** Loads another client resource by path, resolving to its module exports. */
    resource: (path: string) => resources.resource(path),
    /**
     * Defines a component that can be placed in a view as its own component
     * type, once a component library lists it.
     */
    defineComponent,
    /**
     * Defines a component library: the default export of a Component Library
     * resource, listing the components views can use under its namespace.
     */
    defineComponentLibrary,
    /** Describes the props of a library component. */
    prop,
    /** Describes an event of a library component. */
    event,
    /**
     * The context of the library component being rendered. For components
     * rendered inside a library component.
     */
    useComponent,
  }
}
