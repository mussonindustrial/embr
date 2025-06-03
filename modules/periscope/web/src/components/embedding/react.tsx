import React from 'react'
import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentEvents,
  ComponentLifecycleEvents,
  getScriptTransform,
  useDeepCompareMemo,
} from '@embr-js/perspective-client'

import * as Babel from '@babel/standalone'
import { TransformOptions } from '@babel/core'

const COMPONENT_TYPE = 'embr.periscope.embedding.react'

type ReactComponentProps = Record<string, never>

type ReactProps = {
  component: string
  props: ReactComponentProps
  options: {
    babel: TransformOptions
  }
  events: ComponentEvents & {
    target: {
      lifecycle: ComponentLifecycleEvents
    }
  }
  style: StyleObject
}

export function ReactComponent(props: ComponentProps<ReactProps>) {
  const component = useDeepCompareMemo(() => {
    const transpiled = Babel.transform(
      props.props.component,
      props.props.options.babel
    ).code

    if (transpiled == null) {
      return null
    }

    return getScriptTransform(
      props,
      props.store
    )(`(props) => {
      const iife = ${transpiled}
      return iife(props)
    }`) as React.FC<ReactComponentProps>
  }, [props.props.component, props.props.options.babel])

  if (component == null) {
    return null
  }

  return (
    <div {...props.emit()}>
      {React.createElement(component, props.props.props)}
    </div>
  )
}

export class ReactComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class ReactComponentMeta implements ComponentMeta {
  getComponentType(): string {
    return COMPONENT_TYPE
  }

  getDefaultSize(): SizeObject {
    return {
      width: 300,
      height: 300,
    }
  }

  createDelegate(component: AbstractUIElementStore): ComponentStoreDelegate {
    return new ReactComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): ReactProps {
    return {
      component: tree.readString('component', ''),
      props: tree.readObject('props', {}),
      options: tree.readObject('options', {}),
      events: tree.readObject('events', {}),
      style: tree.readStyle('style'),
    } as never
  }

  getViewComponent(): PComponent {
    return ReactComponent as PComponent
  }
}
