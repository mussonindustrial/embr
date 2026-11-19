import {
  createElement,
  CSSProperties,
  Fragment,
  ReactElement,
  ReactNode,
  useContext,
  useMemo,
  useRef,
  useState,
} from 'react'

import { ComponentReactContext, ElementSlot } from './context'
import type {
  Child,
  ComponentContext,
  LibraryComponent,
  LibraryComponentEvent,
  PlacedChild,
} from './periscopeApi'
import { HostedComponent } from './host'
import { toPath, replaceEqualDeep, setIn } from './props'
import { joinClasses } from './root'

type Values = Record<string, unknown>

type ElementProps = Values & {
  className?: string
  style?: CSSProperties
  children?: ReactNode
  placed?: PlacedChild[]
}

type WrittenProps = Record<string, { base: unknown; value: unknown }>

type Position = LibraryComponent['periscope']['position']

function createChild(
  node: ReactNode,
  position: Values,
  key: string,
  index: number
): Child {
  return {
    key,
    index,
    name: key,
    type: '',
    position: position as Child['position'],
    selected: false,
    render: (options) => {
      if (options?.className == null && options?.style == null) {
        return createElement(Fragment, { key }, node)
      }

      const { className, style } = options
      return createElement('div', { key, className, style }, node)
    },
  }
}

function withoutUndefined(values: Values): Values {
  const defined: Values = {}

  for (const [name, value] of Object.entries(values)) {
    if (value !== undefined) {
      defined[name] = value
    }
  }

  return defined
}

function silentEventsOf(events: LibraryComponentEvent[]): Values {
  const silentEvents: Values = {}

  for (const event of events) {
    silentEvents[event.name] = () => {}
  }

  return silentEvents
}

function createChildren(
  position: Position,
  content: ReactNode,
  placed: PlacedChild[] | undefined
): Child[] {
  if (position == null) {
    return []
  }

  const items: PlacedChild[] = []

  if (content != null && content !== false) {
    items.push({ position: {}, node: content, key: 'children' })
  }

  items.push(...(placed ?? []))

  return items.map((item, index) =>
    createChild(
      item.node,
      { ...position.defaults, ...item.position },
      item.key ?? `placed-${index}`,
      index
    )
  )
}

export function renderElement(
  component: LibraryComponent,
  props: ElementProps
): ReactElement | null {
  const { className, style, children: content, placed, ...givenProps } = props
  const { defaults, events, position } = component.periscope
  const outerContext = useContext(ComponentReactContext)
  const [writtenProps, setWrittenProps] = useState<WrittenProps>({})

  const sharedProps = useRef<Values>(givenProps)
  sharedProps.current = replaceEqualDeep(sharedProps.current, givenProps)
  const values = sharedProps.current

  const silentEvents = useMemo(() => silentEventsOf(events), [events])

  const mergedValues = useMemo(() => {
    const next: Values = {
      ...defaults,
      ...silentEvents,
      ...withoutUndefined(values),
    }

    for (const [name, written] of Object.entries(writtenProps)) {
      if (Object.is(written.base, values[name])) {
        next[name] = written.value
      }
    }

    return next
  }, [writtenProps, values, defaults, silentEvents])

  const children = useMemo(
    () => createChildren(position, content, placed),
    [content, placed, position]
  )

  return createElement(HostedComponent, {
    component,
    host: {
      values: mergedValues,
      children,
      ChildSlot: ElementSlot as unknown as ComponentContext['ChildSlot'],
      mode: outerContext?.mode ?? 'session',
      attributesKey: JSON.stringify([className, style]),
      write: (path, value) => {
        const [rootName, ...rest] = toPath(path)

        setWrittenProps((before) => ({
          ...before,
          [rootName]: {
            base: values[rootName],
            value: setIn(mergedValues[rootName], rest, value),
          },
        }))
      },
      attributes: (options) => ({
        className: joinClasses(options.className, className),
        style: { ...options.style, ...style },
        ...(options.ref == null ? {} : { ref: options.ref }),
      }),
      perspective: () => {
        if (outerContext == null) {
          throw new Error(
            'Perspective props are only available inside a library component'
          )
        }

        return outerContext.perspective
      },
    },
  })
}
