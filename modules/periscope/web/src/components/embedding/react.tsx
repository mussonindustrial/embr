import React, {
  MutableRefObject,
  useCallback,
  useEffect,
  useMemo,
  useRef,
} from 'react'
import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  JsObject,
  PComponent,
  PlainObject,
  PropertyTree,
  SizeObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentDelegateJavaScriptProxy,
  ComponentEvents,
  ComponentLifecycleEvents,
  getScriptTransform,
  JavaScriptRunEvent,
  useComponentEvents,
  useDeepCompareMemo,
  useRefLifecycleEvents,
} from '@embr-js/perspective-client'
import { transformProps } from '@embr-js/utils'

import { ClientResourceComponent } from '@/extensions'

const COMPONENT_TYPE = 'embr.periscope.embedding.react'

type ReactComponentProps = Record<string, never>

type ReactProps = {
  resourcePath: string
  component: string
  props: ReactComponentProps
  events: ComponentEvents & {
    component: {
      lifecycle: ComponentLifecycleEvents
    }
  }
  style: StyleObject
}

export function ReactComponent(props: ComponentProps<ReactProps>) {
  const ref: MutableRefObject<unknown> = useRef(undefined)

  // Register the reference with the component delegate
  useEffect(() => {
    const delegate = props.store.delegate as ReactComponentDelegate
    delegate.setProxyRef(ref.current as never)
  }, [props.store.delegate, ref.current])

  const writeProps = useCallback(
    (path: string, value: unknown) => {
      props.store.props.write(`props.${path}`, value)
    },
    [props.props]
  )

  const transformedProps = useMemo(() => {
    return transformProps(props.props, [
      getScriptTransform(props, props.store),
    ]) as ReactProps
  }, [props.props])

  // Apply transforms to the user-supplied properties
  const innerProps = useDeepCompareMemo(() => {
    const transformedProps = transformProps(props.props.props, [
      getScriptTransform(props, props.store),
    ]) as ReactComponentProps

    return {
      ...transformedProps,
      ref,
      renderContext: props,
    } as unknown
  }, [props.props.props, ref]) as ReactComponentProps

  useComponentEvents(props.store, transformedProps.events, ref.current)
  useRefLifecycleEvents(
    transformedProps.events?.component?.lifecycle ?? {},
    ref.current
  )

  return (
    <div {...props.emit()}>
      <ClientResourceComponent
        resourcePath={props.props.resourcePath}
        component={props.props.component}
        props={innerProps}
        writeProps={writeProps}
        thisArg={props}
      />
    </div>
  )
}

export class ReactComponentDelegate extends ComponentStoreDelegate {
  private proxy = new ComponentDelegateJavaScriptProxy(this)

  setProxyRef(ref?: object) {
    this.proxy.setRef(ref)
  }

  handleEvent(eventName: string, eventObject: JsObject) {
    if (this.proxy.handles(eventName)) {
      this.proxy.handleEvent(eventObject as JavaScriptRunEvent)
    }
  }

  mapStateToProps(): PlainObject {
    return this
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
      resourcePath: tree.readString('resourcePath', ''),
      component: tree.readString('component', 'default'),
      props: tree.readObject('props', {}),
      events: tree.readObject('events', {}),
      style: tree.readStyle('style'),
    } as never
  }

  getViewComponent(): PComponent {
    return ReactComponent as PComponent
  }
}
