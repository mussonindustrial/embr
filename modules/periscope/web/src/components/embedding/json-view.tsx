import React, { useEffect, useLayoutEffect, useRef } from 'react'
import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStore,
  ComponentStoreDelegate,
  Emitter,
  JsObject,
  PageStore,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
  ViewDefinition,
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'
import { JoinableView } from '../../util'
import { getClientStore } from '@embr-js/perspective-client'

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

function MissingComponentDelegate({ emit }: { emit: Emitter }) {
  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <ViewStateDisplay
        primaryMessage="View Failed to Load"
        secondaryMessage={`No component delegate was found`}
        icon={
          <svg className="view-state-icon">
            <use xlinkHref="/res/perspective/icons/material-icons.svg#warning" />
          </svg>
        }
      />
    </div>
  )
}

export function JsonViewComponent({
  props,
  store,
  emit,
}: ComponentProps<JsonViewProps>) {
  const mountPath = getChildMountPath(store)

  if (store.delegate == null) {
    console.warn(
      `No delegate found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate emit={emit} />
  }

  const resourcePath = 'JSON_VIEW'

  const clientStore = getClientStore()
  if (clientStore == undefined) {
    console.warn(
      `No client store found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate emit={emit} />
  }

  const isMounted = useRef(false)

  if (!isMounted.current) {
    console.log('Registering view definition.')
    const views = clientStore.resources.project?.views
    views?.set(resourcePath, props.viewJson)

    if (clientStore.isClient) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.resources.project.subscriptionMap[resourcePath] = []
    } else {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.page.viewDefCache[resourcePath] = props.viewJson
    }
  }

  useEffect(() => {
    isMounted.current = true
  }, [])

  useLayoutEffect(() => {
    console.log('We need to re-register the view definition.')
    const views = clientStore.resources.project?.views
    views?.set(resourcePath, props.viewJson)

    if (clientStore.isClient) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.resources.project.subscriptionMap[resourcePath] = []
    } else {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.page.viewDefCache[resourcePath] = props.viewJson
    }

    console.log('resources', store.view.page.parent.resources)
    console.log('project', store.view.page.parent.resources.project)
    console.log('views', views)

    return () => {
      views?.delete(resourcePath)
    }
  }, [store.view.page.parent.resources.project])

  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <JoinableView
        key={PageStore.instanceKeyFor(resourcePath, mountPath)}
        store={store.view.page.parent}
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
