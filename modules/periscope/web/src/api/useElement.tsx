import React, {
  Fragment,
  ReactElement,
  ReactNode,
  useContext,
  useMemo,
  useRef,
  useState,
} from 'react'

import { ComponentReactContext, ElementSlot } from './context'
import { detachedDelegate } from './delegateEvents'
import { createAttributesKey, HostedComponent } from './HostedComponent'
import type {
  Child,
  ElementProps,
  PlacedChild,
  ProjectComponent,
  ProjectComponentEvent,
} from './periscopeApi'
import { joinClasses } from './Root'
import type { Values } from './types'
import { replaceEqualDeep, rootWrite } from './useProp'

type WrittenProps = Record<string, { base: unknown; value: unknown }>

type Position = ProjectComponent['periscope']['position']

function createElementChild(
  node: ReactNode,
  index: number,
  position: Values,
  key: string
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
        return <Fragment key={key}>{node}</Fragment>
      }

      const { className, style } = options
      return (
        <div key={key} className={className} style={style}>
          {node}
        </div>
      )
    },
  }
}

function withoutUndefined(values: Values): Values {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined)
  )
}

function readSilentEvents(events: ProjectComponentEvent[]): Values {
  return Object.fromEntries(events.map((event) => [event.name, () => {}]))
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
    createElementChild(
      item.node,
      index,
      { ...position.defaults, ...item.position },
      item.key ?? `placed-${index}`
    )
  )
}

export function useElement(
  component: ProjectComponent,
  props: ElementProps
): ReactElement | null {
  const { className, style, children: content, placed, ...givenProps } = props
  const { defaults, events, position } = component.periscope
  const outerContext = useContext(ComponentReactContext)
  const [writtenProps, setWrittenProps] = useState<WrittenProps>({})

  const previousProps = useRef<Values>(givenProps)
  previousProps.current = replaceEqualDeep(previousProps.current, givenProps)
  const values = previousProps.current

  const silentEvents = useMemo(() => readSilentEvents(events), [events])

  const mergedValues = useMemo(() => {
    const next: Values = {
      ...defaults,
      ...silentEvents,
      ...withoutUndefined(values),
    }

    Object.entries(writtenProps).forEach(([name, written]) => {
      if (Object.is(written.base, values[name])) {
        next[name] = written.value
      }
    })

    return next
  }, [writtenProps, values, defaults, silentEvents])

  const children = useMemo(
    () => createChildren(position, content, placed),
    [content, placed, position]
  )

  return (
    <HostedComponent
      component={component}
      host={{
        values: mergedValues,
        children,
        ChildSlot: ElementSlot,
        mode: outerContext?.mode ?? 'session',
        delegate: detachedDelegate,
        attributesKey: createAttributesKey(className, style),
        write: (path, value) => {
          const [name, written] = rootWrite(mergedValues, path, value)

          setWrittenProps((before) => ({
            ...before,
            [name]: { base: values[name], value: written },
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
              'Perspective props are only available inside a React Component.'
            )
          }

          return outerContext.perspective
        },
      }}
    />
  )
}
