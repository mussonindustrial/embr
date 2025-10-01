import {
  ComponentDefinition,
  ComponentInstanceDef,
  ComponentMeta,
  ComponentProps,
  layoutCallbackCreator,
  PComponent,
  PlainObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentDesignDelegate,
  ContainerDesignDelegate,
} from '@inductiveautomation/perspective-designer/build/dist/typedefs/api/interaction/InteractionDelegates'
import React, { createElement, useMemo, useRef } from 'react'
import { transformProps } from '@embr-js/utils'
import {
  ComponentEvents,
  ComponentLifecycleEvents,
  getScriptTransform,
  useComponentEvents,
  useRefLifecycleEvents,
} from '@embr-js/perspective-client'
import {
  DesignerComponentStore,
  PreferredLocation,
  SelectionStore,
} from '@inductiveautomation/perspective-designer'

export type MuijoyComponent = {
  meta: ComponentMeta
  designDelegate?: ComponentDesignDelegate
}

export type BaseComponentProps = PlainObject & {
  events: ComponentEvents & {
    target: {
      lifecycle: ComponentLifecycleEvents
    }
  }
  style: StyleObject
}

export function makeComponent<C extends React.ComponentType>(
  id: string,
  componentType: C
): MuijoyComponent {
  type PerspectiveComponentProps = React.ComponentProps<C> & BaseComponentProps

  const component = (props: ComponentProps<PerspectiveComponentProps>) => {
    const ref = useRef<Element | null>(null)

    const transformedProps = useMemo(() => {
      return transformProps(props.props, [
        getScriptTransform(props, props.store),
      ]) as PerspectiveComponentProps
    }, [props.props])

    useComponentEvents(props.store, transformedProps.events, ref.current)
    useRefLifecycleEvents(
      transformedProps.events?.target?.lifecycle ?? {},
      ref.current
    )

    return (
      <div {...props.emit()}>
        {createElement(
          componentType,
          {
            ...transformedProps,
            ref,
          },
          ...props.store.children.map((componentStore, index) => {
            const Component = componentStore.getComponent()
            const layout = layoutCallbackCreator.forStyle(() => ({}))
            return <Component key={index} layout={layout} />
          })
        )}
      </div>
    )
  }

  const meta: ComponentMeta = {
    getComponentType: () => id,
    getDefaultSize: () => ({
      width: 200,
      height: 200,
    }),
    getViewComponent: () => component as PComponent,
    getPropsReducer: (tree) => {
      return tree.readObject('')
    },
  }

  const designDelegate: ContainerDesignDelegate = {
    type: id,
    isContainer: true,

    addNewComponents: (
      compDefs: ComponentDefinition[],
      _selection: SelectionStore,
      dropContainer: DesignerComponentStore,
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      _preferredLocation?: PreferredLocation | undefined
    ): ComponentInstanceDef[] => {
      return compDefs.map((compDef) => {
        return {
          component: compDef,
          addressPath: dropContainer._addComponent(compDef),
        }
      })
    },
  }

  return {
    meta,
    designDelegate,
  }
}

export const TestComponent = (props: PlainObject) => {
  return <div>Test Component: {JSON.stringify(props, null, 2)}</div>
}
