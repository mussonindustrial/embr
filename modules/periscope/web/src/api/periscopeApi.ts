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
import { defineProjectComponent } from './defineProjectComponent'
import type { PropSetter } from './useProp'
import {
  createEvent,
  createProp,
  defaultsOf,
  JsonSchema,
  objectSchema,
  STYLE_SCHEMA,
} from './schema'
import type { Bounds, Size } from './types'

export type { PropSetter } from './useProp'
export { describeComponent } from './defineProjectComponent'
export { componentOf } from './component'

/**
 * A prop of a React Component: its type, and how the Designer edits it.
 *
 * A component always receives a prop that has a `default()`. Any other prop
 * may be missing from a view, so the component receives it as optional.
 * Inside an `object`, `array` or event, a field is required unless it is
 * `optional()` or has a `default()`.
 */
export interface Prop<T> {
  /** The JSON schema the Designer edits the prop with. */
  readonly schema: JsonSchema
  /** Whether the prop was made `optional()`. */
  readonly isOptional: boolean
  /** Whether the prop has a `default()`. */
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

/** A number prop, with the limits the Designer enforces. */
export interface NumberProp<T extends number | null | undefined = number>
  extends Prop<T> {
  optional(): NumberProp<T | undefined>
  nullable(): NumberProp<T | null> & Pick<this, 'hasDefault'>
  /** The lowest value allowed. */
  min(value: number): this
  /** The highest value allowed. */
  max(value: number): this
  /** The value must be a multiple of the step. */
  step(value: number): this
  /** Only whole numbers are valid. */
  int(): this
}

/** The props of a React Component, by name. */
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

/** An event of a React Component, which the Designer can attach scripts to. */
export interface ComponentEvent<T> {
  readonly schema: JsonSchema
  readonly description?: string
  /** Never set. Carries the type of the event object. */
  readonly type?: T
  /** Shown beside the event in the Designer. */
  describe(description: string): ComponentEvent<T>
}

/** The events of a React Component, by name. */
export type EventSchema = Record<string, ComponentEvent<unknown>>

/** The functions a component receives to fire its events. */
export type InferEvents<E extends EventSchema> = {
  [K in keyof E]: E[K] extends ComponentEvent<infer T>
    ? keyof T extends never
      ? () => void
      : (event: T) => void
    : never
}

type Literal = string | number | boolean | null

/**
 * Describes the props of a React Component.
 *
 * Each prop is built by chaining, such as
 * `prop.number().min(0).default(10).describe('The row count.')`.
 */
export const prop = {
  /** Text, edited as a string. */
  string: () => createProp<Prop<string>>({ type: 'string' }),
  /** A number, which can be given limits. */
  number: () => createProp<NumberProp>({ type: 'number' }),
  /** True or false, edited with a checkbox. */
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
  style: () => createProp<Prop<StyleObject> & Defaulted>(STYLE_SCHEMA),
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
 * Describes an event of a React Component.
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
  /** A ref to the element, merged with the one Perspective sets. */
  ref?: Ref<HTMLElement> | Ref<SVGElement>
} & Omit<ComponentPropsWithoutRef<E>, 'as' | 'asChild'>

/** The class and style a child is rendered with. */
export type ChildRenderOptions = {
  /** Added to the classes of the child's outermost element. */
  className?: string
  /** Set over the style of the child's outermost element. */
  style?: CSSProperties
}

/** A child the Designer is dropping, moving or resizing in a child slot. */
export type PositionChange = {
  /**
   * Where the child is in the child slot and how big it is: the corner
   * nearest the slot's top left, and its size, in the pixels of the slot.
   */
  bounds: Bounds
  /** The size of the child slot. */
  slot: Size
  /** Why the position is changing. */
  reason: 'drop' | 'move' | 'resize'
}

/** A handle that resizes a child in the Designer, by the side or corner it is on. */
export type ResizeHandle = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw'

/** Where a React Component is shown. */
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
  defaultSize?: Size
}

/** A component placed inside a React Component in the Designer. */
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

/** The props of `ChildSlot`: those of the element it renders, and its own. */
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
   * Positions a child the Designer drops, moves or resizes in the slot: returns
   * its position props for the new bounds, such as its coordinates or the cell
   * of a grid. The result is set over the `where` of the slot. On a drop or
   * move, a `width` or `height` equal to the bounds is left out, so a child
   * keeps its size props until it is resized.
   */
  positionChild?: (change: PositionChange) => Partial<InferProps<C>>
  /**
   * Shows handles that resize a child in the Designer, which calls
   * `positionChild` with the new bounds. `true` shows all eight.
   */
  resize?: boolean | ResizeHandle[]
  /** Shown in the Designer while the slot has no children. */
  placeholder?: ReactNode
  /** Renders each child, to style or wrap it. Defaults to `child.render()`. */
  children?: (child: Child<C>) => ReactNode
}

/** The Python delegate that runs on the Gateway for a React Component. */
export type ComponentDelegate = {
  /** Sends an event to the delegate's `handleEvent`. */
  fireEvent(name: string, event?: Record<string, unknown>): void
  /**
   * Calls the handler each time the delegate fires the event. Returns a
   * function that stops listening.
   */
  onEvent<T = Record<string, unknown>>(
    name: string,
    handler: (event: T) => void
  ): () => void
}

/** What a React Component can use besides its props. */
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
   * Sets what `this` is in JavaScript that scripts run with the component's
   * JavaScript proxy, `getJavaScriptProxy()`, while the component is mounted.
   * A React ref is read when the script runs. Without a target, `this` is the
   * component's Root element.
   */
  useJavaScriptProxy(target: unknown): void
  /**
   * Where the component is shown: in a session, in the Designer while it is
   * being designed, or in the Designer's preview.
   */
  readonly mode: ComponentMode
  /** The component's delegate on the Gateway, written in its Delegate script. */
  readonly delegate: ComponentDelegate
  /** The Perspective props of the component, as they are now. */
  readonly perspective: ComponentProps<Record<string, unknown>>
}

/** Renders a React Component from its props. */
export type ComponentRender<
  P,
  S extends PropSchema = PropSchema,
  C extends PropSchema = PropSchema,
> = (props: P, context: ComponentContext<S, C>) => ReactNode

/** What a React Component declares about itself. */
export type ComponentMetadata = {
  /** Shown as the component's tooltip in the component palette. */
  description?: string
  /** The icon shown in the Designer, as `library/name`. */
  icon?: string
  /** The size a new component starts with, where its container uses one. */
  defaultSize?: Size
}

/** What a React Component is defined with. */
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

/** An event of a component made with `defineComponent`. */
export type ProjectComponentEvent = {
  /** The name of the event, and of the function that fires it. */
  name: string
  /** Shown beside the event in the Designer. */
  description?: string
  /** The JSON schema of the event object. */
  schema: JsonSchema
}

/** A child given to a React Component that is rendered as a React element. */
export type PlacedChild<C = Record<string, unknown>> = {
  /** The position props it is placed with, over their defaults. */
  position: Partial<C>
  /** The React node to show as the child. */
  node: ReactNode
  /** Stable for the child, for use as its React key. */
  key?: string
}

/** The props of a React Component that is rendered as a React element. */
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
 * A component made with `defineComponent`. It is the default export of a
 * Component resource, and other components render it as a React element.
 */
export type ProjectComponent<
  P = Record<string, unknown>,
  C = Record<string, unknown>,
> = ElementFunction<P, C> & {
  displayName?: string
  render: ComponentRender<never>
  periscope: ComponentMetadata & {
    schema: JsonSchema & { properties: Record<string, JsonSchema> }
    defaults: Record<string, unknown>
    events: ProjectComponentEvent[]
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

type DefineComponent = <
  S extends PropSchema = Record<never, never>,
  E extends EventSchema = Record<never, never>,
  C extends PropSchema = Record<never, never>,
>(
  definition: ComponentDefinition<S, E, C>
) => ProjectComponent<InferProps<S> & InferEvents<E>, InferProps<C>>

/**
 * The Perspective props of the React (Legacy) component.
 *
 * @deprecated Make a Component resource with `defineComponent` instead.
 */
export type ReactComponentContext<P = Record<string, unknown>> =
  ComponentProps<{
    resource: { path: string; export?: string }
    props: P
    style: StyleObject
  }>

/**
 * The props the React (Legacy) component passes to the component it renders.
 *
 * @deprecated Make a Component resource with `defineComponent` instead.
 */
export type ReactComponentProps<P = Record<string, unknown>> = P & {
  /** Writes a value to the React component's `props`, at a path inside them. */
  writeProps: (path: string, value: unknown) => void
  /** The Perspective props of the React component, also bound as `this`. */
  renderContext: ReactComponentContext<P>
}

/**
 * A component in a client resource, rendered by the React (Legacy) component.
 *
 * `P` is the shape of the React component's `props`. Written as a `function`,
 * the component has the React component's Perspective props as `this`.
 *
 * @deprecated Make a Component resource with `defineComponent` instead.
 */
export type ReactComponent<P = Record<string, unknown>> = (
  this: ReactComponentContext<P>,
  props: ReactComponentProps<P>
) => ReactNode

type Resources = {
  resource(path: string): Promise<ESModule>
  source?(): string | undefined
}

export function createPeriscopeApi(resources: Resources) {
  const defineComponent: DefineComponent = ((
    definition: ComponentDefinition
  ) => {
    const component = defineProjectComponent(definition)
    component.periscope.source = resources.source?.()

    return component
  }) as never

  return {
    /** Shows a toast notification in the Perspective session. */
    toast,
    /** Loads another client resource by path, resolving to its module exports. */
    resource: (path: string) => resources.resource(path),
    /**
     * Defines a component: the default export of a Component resource, which
     * views use as a component type of its own.
     */
    defineComponent,
    /** Describes the props of a React Component. */
    prop,
    /** Describes an event of a React Component. */
    event,
    /**
     * The context of the React Component being rendered. For components
     * rendered inside a React Component.
     */
    useComponent,
  }
}
