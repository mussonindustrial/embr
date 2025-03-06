import React, { useEffect, useRef } from 'react'
import {
  AbstractUIElementStore,
  ClientStore,
  ComponentMeta,
  ComponentProps,
  ComponentStore,
  ComponentStoreDelegate,
  Emitter,
  JsObject,
  ObservableProjectDefinition,
  PageStore,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
  ViewDefinition,
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'
import { JoinableView } from '../../util'

const COMPONENT_TYPE = 'embr.periscope.embedding.json-view'

type JsonViewProps = {
  viewJson: ViewDefinition
  viewParams: JsObject
  viewStyle: StyleObject
  useDefaultHeight: boolean
  useDefaultMinHeight: boolean
  useDefaultMinWidth: boolean
  useDefaultWidth: boolean
}

function getChildMountPath(store: ComponentStore) {
  return `${store.viewMountPath}.${store.addressPathString}`
}

function getSubscriptionMap(project: ObservableProjectDefinition) {
  return Reflect.get(project, 'subscriptionMap')
}

function getViewDefCache(page: PageStore) {
  return Reflect.get(page, 'viewDefCache') as Map<string, ViewDefinition>
}

function FailedToLoadView({
  emit,
  message,
}: {
  emit: Emitter
  message: string
}) {
  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <ViewStateDisplay
        primaryMessage="View Failed to Load"
        secondaryMessage={message}
        icon={
          <svg className="view-state-icon">
            <use xlinkHref="/res/perspective/icons/material-icons.svg#warning" />
          </svg>
        }
      />
    </div>
  )
}

function installView(
  clientStore: ClientStore,
  resourcePath: string,
  viewJson: ViewDefinition
) {
  const views = clientStore.resources.project?.views

  views?.set(resourcePath, viewJson)

  if (clientStore.isClient && clientStore.resources.project) {
    getSubscriptionMap(clientStore.resources.project)[resourcePath] = []
  }
  if (clientStore.isDesigner) {
    getViewDefCache(clientStore.page).set(resourcePath, viewJson)
  }
}

function uninstallView(clientStore: ClientStore, resourcePath: string) {
  const views = clientStore.resources.project?.views

  views?.delete(resourcePath)

  if (clientStore.isClient && clientStore.resources.project) {
    delete getSubscriptionMap(clientStore.resources.project)[resourcePath]
  }
  if (clientStore.isDesigner) {
    getViewDefCache(clientStore.page).delete(resourcePath)
  }
}

export function JsonViewComponent({
  props,
  store,
  emit,
}: ComponentProps<JsonViewProps>) {
  const viewRef = useRef<JoinableView>(null)
  const clientStore = store.clientStore
  const mountPath = getChildMountPath(store)
  const resourcePath = `${store.view.resourcePath}.${store.addressPathString}`

  if (store.delegate == null) {
    console.warn(
      `No delegate found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return (
      <FailedToLoadView
        emit={emit}
        message="No componenet delegate was found"
      />
    )
  }

  if (clientStore == undefined) {
    console.warn(
      `No client store found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <FailedToLoadView emit={emit} message="No client store was found" />
  }

  // Reinstall the view whenever the definition changes.
  let viewJsonDependency: ViewDefinition | string = props.viewJson

  // If we are in the designer, use a string representation of the view.
  // Without this fix, the view will re-render when the `viewParams` changes.
  if (clientStore.isDesigner) {
    viewJsonDependency = JSON.stringify(props.viewJson)
  }
  useEffect(() => {
    if (isMounted.current) {
      console.debug(
        'View definition changed, re-registering the view definition.'
      )
      installView(clientStore, resourcePath, props.viewJson)
      viewRef.current?.resetInstance()
    }

    return () => {
      uninstallView(clientStore, resourcePath)
    }
  }, [viewJsonDependency])

  // Create the view of startup, before the first render.
  const isMounted = useRef(false)
  if (!isMounted.current) {
    console.debug('Registering view definition.')
    installView(clientStore, resourcePath, props.viewJson)
  }
  useEffect(() => {
    isMounted.current = true
  }, [])

  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <JoinableView
        ref={viewRef}
        key={PageStore.instanceKeyFor(resourcePath, mountPath)}
        store={clientStore}
        resourcePath={resourcePath}
        mountPath={mountPath}
        params={props.viewParams}
        useDefaultHeight={props.useDefaultHeight}
        useDefaultMinHeight={props.useDefaultMinHeight}
        useDefaultMinWidth={props.useDefaultMinWidth}
        useDefaultWidth={props.useDefaultWidth}
        rootStyle={{
          ...props.viewStyle,
          classes: props.viewStyle.classes,
        }}
        delegate={store.delegate}
      />
    </div>
  )
}

export class JsonViewComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class JsonViewComponentMeta implements ComponentMeta {
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
    return new JsonViewComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): JsonViewProps {
    return {
      viewJson: tree.readObject('viewJson', {}),
      viewStyle: tree.readStyle('viewStyle'),
      useDefaultHeight: tree.readBoolean('useDefaultHeight'),
      useDefaultMinHeight: tree.readBoolean('useDefaultMinHeight'),
      useDefaultMinWidth: tree.readBoolean('useDefaultMinWidth'),
      useDefaultWidth: tree.readBoolean('useDefaultWidth'),
      style: tree.readStyle('style'),
    } as never
  }

  getViewComponent(): PComponent {
    return JsonViewComponent as PComponent
  }
}
