import React, {
  ComponentType,
  lazy,
  MutableRefObject,
  Suspense,
  useMemo,
  useRef,
} from 'react'
import type { PlainObject } from '@inductiveautomation/perspective-client'
import {
  ComponentEvents,
  getScriptTransform,
  useComponentEvents,
} from '@embr-js/perspective-client'
import { transformProps } from '@embr-js/utils'

import { HostedComponent } from '@/api/host'
import type { ComponentContext, LibraryComponent } from '@/api/periscopeApi'
import { toPath, replaceEqualDeep, setIn } from '@/api/props'
import { FailedToLoadComponent } from '@/extensions'
import { waitForClientResourceStore } from '@/util'
import { ChildSlot, useChildren, useSelectedChild } from './children'
import { ErrorBoundary } from './ErrorBoundary'
import { currentMode, rootAttributes, Perspective, Values } from './shared'

type PerspectiveRef = MutableRefObject<Perspective>

type ViewProps = {
  perspective: Perspective
  values: Values
}

type ResolvedProps = ViewProps & {
  component: LibraryComponent
}

type FailedProps = {
  perspective: Perspective
  message: string
}

type EventFunctions = Record<string, (event?: PlainObject) => void>

function FailedComponent({ perspective, message }: FailedProps) {
  return (
    <div {...rootAttributes(perspective)}>
      <FailedToLoadComponent message={message} />
    </div>
  )
}

function useEventFunctions(
  component: LibraryComponent,
  perspective: PerspectiveRef
): EventFunctions {
  return useMemo(() => {
    const functions: EventFunctions = {}

    for (const { name } of component.periscope.events) {
      functions[name] = (event?: PlainObject) => {
        const { componentEvents } = perspective.current
        return componentEvents.fireComponentEvent(name, event ?? {})
      }
    }

    return functions
  }, [component, perspective])
}

function usePerspectiveChildren(
  component: LibraryComponent,
  perspective: Perspective
) {
  const children = useChildren(
    perspective.store,
    component.periscope.position?.defaults
  )
  const selectedChild = useSelectedChild(children)

  return useMemo(
    () =>
      children.handles.map((child) =>
        child === selectedChild ? { ...child, selected: true } : child
      ),
    [children, selectedChild]
  )
}

function writeProp(
  component: LibraryComponent,
  perspective: Perspective,
  values: Values,
  path: string,
  value: unknown
) {
  const props = perspective.store.props
  const [rootName, ...rest] = toPath(path)

  if (rest.length > 0 && !(rootName in values)) {
    const defaultValue = component.periscope.defaults[rootName]
    props.write(rootName, setIn(defaultValue, rest, value))
  } else {
    props.write(path, value)
  }
}

function ResolvedComponent({ component, perspective, values }: ResolvedProps) {
  const latest = useRef(perspective)
  latest.current = perspective

  const children = usePerspectiveChildren(component, perspective)
  const eventFunctions = useEventFunctions(component, latest)

  const props = useMemo(
    () => ({ ...component.periscope.defaults, ...values, ...eventFunctions }),
    [component, values, eventFunctions]
  )

  const attributes = rootAttributes(perspective)

  return (
    <ErrorBoundary
      resetKey={props}
      fallback={(message) => (
        <FailedComponent perspective={perspective} message={message} />
      )}
    >
      <HostedComponent
        component={component}
        host={{
          values: props,
          children,
          ChildSlot: ChildSlot as unknown as ComponentContext['ChildSlot'],
          mode: currentMode(),
          attributesKey: JSON.stringify([
            attributes.className,
            attributes.style,
          ]),
          write: (path, value) =>
            writeProp(component, latest.current, values, path, value),
          attributes: (options) => rootAttributes(latest.current, options),
          perspective: () => latest.current,
        }}
      />
    </ErrorBoundary>
  )
}

function resolveComponent(id: string): ComponentType<ViewProps> {
  return lazy(async () => {
    const store = await waitForClientResourceStore()
    const component = await store.libraryComponent(id)

    if (component == null) {
      return {
        default: ({ perspective }: ViewProps) => (
          <FailedComponent
            perspective={perspective}
            message={`Component '${id}' not found`}
          />
        ),
      }
    }

    return {
      default: (props: ViewProps) => (
        <ResolvedComponent component={component} {...props} />
      ),
    }
  }) as ComponentType<ViewProps>
}

export function createViewComponent(id: string) {
  const Resolved = resolveComponent(id)

  return function LibraryViewComponent(props: Perspective) {
    const transformed = useMemo(
      () =>
        transformProps(props.props, [
          getScriptTransform(props, props.store),
        ]) as Values & { events?: ComponentEvents },
      [props.props]
    )

    useComponentEvents(props.store, transformed.events ?? {}, undefined)

    const previousValues = useRef<Values>({})

    const values = useMemo(() => {
      const next: Values = { ...transformed }
      delete next.events
      delete next.style

      previousValues.current = replaceEqualDeep(previousValues.current, next)
      return previousValues.current
    }, [transformed])

    return (
      <Suspense fallback={<div {...rootAttributes(props)} />}>
        <Resolved perspective={props} values={values} />
      </Suspense>
    )
  }
}
