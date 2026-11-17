import React, {
  memo,
  MutableRefObject,
  ReactElement,
  ReactNode,
  useEffect,
  useLayoutEffect,
  useMemo,
  useRef,
} from 'react'

import { ComponentReactContext } from './context'
import type {
  Child,
  ComponentContext,
  ComponentMode,
  LibraryComponent,
} from './periscopeApi'
import { createPropStore, PropContext, useProp } from './props'
import { Root, RootContext, type RootHost, type RootOptions } from './root'

type Values = Record<string, unknown>

export type ComponentHost = {
  values: Values
  children: Child[]
  ChildSlot: ComponentContext['ChildSlot']
  mode: ComponentMode
  attributesKey: unknown
  write(path: string, value: unknown): void
  attributes(options: RootOptions): object
  perspective(): ComponentContext['perspective']
}

type HostProps = {
  component: LibraryComponent
  host: ComponentHost
}

type RenderedComponentProps = {
  component: LibraryComponent
  values: Values
  context: ComponentContext
  roots: MutableRefObject<number>
}

const useIsomorphicLayoutEffect =
  typeof window === 'undefined' ? useEffect : useLayoutEffect

function isEmpty(node: ReactNode) {
  return node == null || typeof node === 'boolean'
}

const RenderedComponent = memo(function RenderedComponent({
  component,
  values,
  context,
  roots,
}: RenderedComponentProps) {
  const node = component.render(values as never, context)
  const empty = isEmpty(node)

  useIsomorphicLayoutEffect(() => {
    if (!empty && roots.current !== 1) {
      throw new Error(
        `'${component.periscope.id}' must render one Root as its outermost element`
      )
    }
  })

  return <>{node}</>
})

export function HostedComponent({ component, host }: HostProps): ReactElement {
  const latest = useRef(host)
  latest.current = host
  const roots = useRef(0)

  const propStore = useMemo(
    () => createPropStore((path, value) => latest.current.write(path, value)),
    []
  )

  propStore.props = host.values
  useEffect(() => propStore.notify())

  const mount = useMemo(
    () => () => {
      roots.current += 1
      return () => {
        roots.current -= 1
      }
    },
    []
  )

  const rootHost = useMemo<RootHost>(
    () => ({
      attributes: (options) => latest.current.attributes(options),
      mount,
    }),
    [mount, host.attributesKey]
  )

  const context = useMemo<ComponentContext>(
    () => ({
      children: host.children,
      ChildSlot: host.ChildSlot,
      Root: Root as unknown as ComponentContext['Root'],
      useProp: useProp as ComponentContext['useProp'],
      mode: host.mode,
      get perspective() {
        return latest.current.perspective()
      },
    }),
    [host.children, host.ChildSlot, host.mode]
  )

  return (
    <PropContext.Provider value={propStore}>
      <ComponentReactContext.Provider value={context}>
        <RootContext.Provider value={rootHost}>
          <RenderedComponent
            component={component}
            values={host.values}
            context={context}
            roots={roots}
          />
        </RootContext.Provider>
      </ComponentReactContext.Provider>
    </PropContext.Provider>
  )
}
