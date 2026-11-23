import React, {
  ComponentType,
  lazy,
  MutableRefObject,
  Suspense,
  useMemo,
  useRef,
} from 'react'
import {
  ComponentStoreDelegate,
  PlainObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentEvents,
  getScriptTransform,
  useComponentEvents,
} from '@embr-js/perspective-client'
import { transformProps } from '@embr-js/utils'

import {
  attributesKeyOf,
  ComponentDelegate,
  createDelegateEvents,
  detachedDelegate,
  HostedComponent,
  ProjectComponent,
  replaceEqualDeep,
  rootWrite,
  Values,
} from '@/api'
import { FailedToLoadComponent } from '@/extensions/client-resource'
import { waitForClientResourceStore } from '@/util'
import { rootAttributes } from './attributes'
import { ChildSlot, useChildren, useSelectedChild } from './children'
import { currentMode } from './designer-state'
import { ErrorBoundary } from './error-boundary'
import type { PerspectiveProps } from './types'

type PerspectiveRef = MutableRefObject<PerspectiveProps>

type ProjectComponentViewProps = {
  perspective: PerspectiveProps
  values: Values
}

type ResolvedComponentProps = ProjectComponentViewProps & {
  component: ProjectComponent
}

type FailedComponentProps = {
  perspective: PerspectiveProps
  message: string
}

type EventFunctions = Record<string, (event?: PlainObject) => void>

function FailedComponent({ perspective, message }: FailedComponentProps) {
  return (
    <div {...rootAttributes(perspective)}>
      <FailedToLoadComponent message={message} />
    </div>
  )
}

function useEventFunctions(
  component: ProjectComponent,
  perspective: PerspectiveRef
): EventFunctions {
  return useMemo(
    () =>
      Object.fromEntries(
        component.periscope.events.map(({ name }) => [
          name,
          (event?: PlainObject) => {
            const { componentEvents } = perspective.current
            return componentEvents.fireComponentEvent(name, event ?? {})
          },
        ])
      ),
    [component, perspective]
  )
}

function usePerspectiveChildren(
  component: ProjectComponent,
  perspective: PerspectiveProps
) {
  const children = useChildren(
    perspective.store,
    component.periscope.position?.defaults
  )
  const selectedChild = useSelectedChild(children)

  return useMemo(
    () =>
      children.items.map((child) =>
        child === selectedChild ? { ...child, selected: true } : child
      ),
    [children, selectedChild]
  )
}

function writeProp(
  component: ProjectComponent,
  perspective: PerspectiveProps,
  values: Values,
  path: string,
  value: unknown
) {
  const props = perspective.store.props
  const [name, written] = rootWrite(component.periscope.defaults, path, value)

  if (name in values) {
    props.write(path, value)
  } else {
    props.write(name, written)
  }
}

function delegateOf(perspective: PerspectiveProps): ComponentDelegate {
  const delegate = perspective.store.delegate

  if (delegate instanceof ProjectComponentDelegate) {
    return delegate.componentDelegate
  }

  return detachedDelegate
}

function ResolvedComponent({
  component,
  perspective,
  values,
}: ResolvedComponentProps) {
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
          ChildSlot,
          mode: currentMode(),
          delegate: delegateOf(perspective),
          attributesKey: attributesKeyOf(
            attributes.className,
            attributes.style
          ),
          write: (path, value) =>
            writeProp(component, latest.current, values, path, value),
          attributes: (options) => rootAttributes(latest.current, options),
          perspective: () => latest.current,
        }}
      />
    </ErrorBoundary>
  )
}

function resolveComponent(
  id: string
): ComponentType<ProjectComponentViewProps> {
  return lazy(async () => {
    const store = await waitForClientResourceStore()
    const component = await store.getComponent(id)

    if (component == null) {
      return {
        default: ({ perspective }: ProjectComponentViewProps) => (
          <FailedComponent
            perspective={perspective}
            message={`Component '${id}' not found.`}
          />
        ),
      }
    }

    return {
      default: (props: ProjectComponentViewProps) => (
        <ResolvedComponent component={component} {...props} />
      ),
    }
  }) as ComponentType<ProjectComponentViewProps>
}

export function createProjectComponent(id: string) {
  const Resolved = resolveComponent(id)

  return function ProjectViewComponent(props: PerspectiveProps) {
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

export class ProjectComponentDelegate extends ComponentStoreDelegate {
  private readonly events = createDelegateEvents()

  readonly componentDelegate: ComponentDelegate = {
    fireEvent: (name, event = {}) => this.fireEvent(name, event),
    onEvent: this.events.on,
  }

  handleEvent(eventName: string, eventObject: Values): void {
    this.events.emit(eventName, eventObject)
  }
}
