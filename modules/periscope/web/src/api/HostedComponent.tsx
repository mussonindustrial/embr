import React, {
  memo,
  MutableRefObject,
  ReactElement,
  ReactNode,
  useCallback,
  useEffect,
  useMemo,
  useRef,
} from 'react'
import { useIsomorphicLayoutEffect } from '@embr-js/utils'

import { ComponentReactContext } from './context'
import type {
  Child,
  ComponentContext,
  ComponentDelegate,
  ComponentMode,
  ProjectComponent,
} from './periscopeApi'
import { Root, RootContext, RootHost, RootOptions } from './Root'
import type { Values } from './types'
import { createPropStore, PropContext, useProp } from './useProp'

/** Where a component's JavaScript proxy finds what `this` is. */
export type JavaScriptProxyHost = {
  setElement(element: Element | null): void
  setTarget(target: unknown): void
}

export type ComponentHost = {
  values: Values
  children: Child[]
  ChildSlot: ComponentContext['ChildSlot']
  mode: ComponentMode
  delegate: ComponentDelegate
  proxy?: JavaScriptProxyHost
  attributesKey: unknown
  write(path: string, value: unknown): void
  attributes(options: RootOptions): object
  perspective(): ComponentContext['perspective']
}

type HostedComponentProps = {
  component: ProjectComponent
  host: ComponentHost
}

type RenderedComponentProps = {
  component: ProjectComponent
  values: Values
  context: ComponentContext
  roots: MutableRefObject<number>
}

function createJavaScriptProxyHook(latest: MutableRefObject<ComponentHost>) {
  return function useJavaScriptProxy(target: unknown) {
    useIsomorphicLayoutEffect(() => {
      latest.current.proxy?.setTarget(target)
      return () => latest.current.proxy?.setTarget(undefined)
    }, [target])
  }
}

export function createAttributesKey(className: unknown, style: unknown) {
  return JSON.stringify([className, style])
}

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
        `'${component.displayName}' must render one Root as its outermost element.`
      )
    }
  })

  return <>{node}</>
})

export function HostedComponent({
  component,
  host,
}: HostedComponentProps): ReactElement {
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

  const element = useCallback(
    (node: Element | null) => latest.current.proxy?.setElement(node),
    []
  )

  const useJavaScriptProxy = useMemo(
    () => createJavaScriptProxyHook(latest),
    []
  )

  const rootHost = useMemo<RootHost>(
    () => ({
      attributes: (options) => latest.current.attributes(options),
      mount,
      element,
    }),
    [mount, element, host.attributesKey]
  )

  const context = useMemo<ComponentContext>(
    () => ({
      children: host.children,
      ChildSlot: host.ChildSlot,
      Root,
      useProp: useProp as ComponentContext['useProp'],
      useJavaScriptProxy,
      mode: host.mode,
      delegate: host.delegate,
      get perspective() {
        return latest.current.perspective()
      },
    }),
    [
      host.children,
      host.ChildSlot,
      host.mode,
      host.delegate,
      useJavaScriptProxy,
    ]
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
