import React, {
  createElement,
  ReactElement,
  useEffect,
  useMemo,
  useRef,
} from 'react'
import {
  ComponentProps,
  layoutCallbackCreator,
  PComponent,
  PlainObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import { transformProps } from '@embr-js/utils'
import {
  ComponentEvents,
  ComponentLifecycleEvents,
  getScriptTransform,
  useComponentEvents,
  useRefLifecycleEvents,
} from '@embr-js/perspective-client'

import {
  MuiJoyDesignDelegate,
  MuijoyComponent,
  MuiJoyComponentMeta,
  MuiJoyComponentStoreDelegate,
} from './MuiJoyComponent'
import { PropsDecorator } from '../utilities'

export type BaseComponentProps = PlainObject & {
  events: ComponentEvents & {
    target: {
      lifecycle: ComponentLifecycleEvents
    }
  }
  style: StyleObject
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function makeComponent<C extends React.ComponentType<any>>(
  type: string,
  componentType: C
): MuijoyComponent {
  type ThisComponentProps = React.ComponentProps<C> & BaseComponentProps

  const component = (props: ComponentProps<ThisComponentProps>) => {
    const ref = useRef<Element>(undefined)

    const transformedProps = useMemo(() => {
      return transformProps(props.props, [
        getScriptTransform(props, props.store),
      ]) as ThisComponentProps
    }, [props.props])

    // Register the component with the component delegate
    useEffect(() => {
      const delegate = props.store.delegate as MuiJoyComponentStoreDelegate
      delegate.setRef(ref.current)
    }, [props.store.delegate, ref.current])

    // Use component event properties
    useComponentEvents(props.store, transformedProps.events, ref.current)
    useRefLifecycleEvents(
      transformedProps.events?.target?.lifecycle ?? {},
      ref.current
    )

    const slotChildren = useMemo(() => {
      return props.store.children.filter(
        (child) => child.componentMeta.getComponentType() == 'embr.muijoy.slot'
      )
    }, [props.store.children])

    // Subscribe to child name changes
    const [, forceUpdate] = React.useReducer((x) => x + 1, 0)
    useEffect(() => {
      const disposers = slotChildren.map((child) => {
        let previousName = child.props.readStringIfExists('name')

        return child.props.subscribe(() => {
          const name = child.props.readStringIfExists('name')
          if (name != previousName) {
            console.log(
              `forcing update because ${child.addressPathString} slot named has changed`
            )
            forceUpdate()
            previousName = name
          }
        })
      })

      return () => disposers.forEach((disposer) => disposer())
    }, [slotChildren])

    const slotProps: Record<string, ReactElement> = {}
    slotChildren.forEach((child) => {
      const name = child.props.readStringIfExists('name')
      if (name == null) {
        return
      }

      const Component = child.getComponent()
      slotProps[name] = (
        <PropsDecorator key={child.addressPathString}>
          <Component layout={layoutCallbackCreator.forStyle(() => ({}))} />
        </PropsDecorator>
      )
    })

    const nonSlotChildren = props.store.children.filter(
      (child) => !slotChildren.includes(child)
    )

    const componentProps = {
      ...transformedProps,
      ...props.emit({}, true),
      ...slotProps,
      forwardRef: ref,
    }

    console.log(props.props, transformedProps, componentProps)

    return createElement(
      componentType,
      componentProps,
      ...nonSlotChildren.map((child) => {
        const Component = child.getComponent()
        return (
          <PropsDecorator key={child.addressPathString}>
            <Component layout={layoutCallbackCreator.forStyle(() => ({}))} />
          </PropsDecorator>
        )
      })
    )
  }

  return {
    meta: new MuiJoyComponentMeta(type, component as PComponent),
    designDelegate: new MuiJoyDesignDelegate(type),
  }
}
