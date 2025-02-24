import React, { useCallback, useEffect, useRef, useState } from 'react'
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
import { isEqual } from 'lodash'

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

  const clientStore = getClientStore()
  if (clientStore == undefined) {
    console.warn(
      `No client store found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate emit={emit} />
  }

  const resourcePath = `${store.view.resourcePath}.${store.addressPathString}`
  const views = clientStore.resources.project?.views

  const installView = useCallback(() => {
    if (clientStore.isClient) {
      views?.set(resourcePath, props.viewJson)
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.resources.project.subscriptionMap[resourcePath] = []
    } else {
      views?.set(resourcePath, props.viewJson)
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.page.viewDefCache.set(resourcePath, props.viewJson)
    }
  }, [props.viewJson])

  const uninstallView = useCallback(() => {
    views?.delete(resourcePath)

    if (clientStore.isDesigner) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      clientStore.page.viewDefCache.delete(resourcePath)
    }
  }, [views])

  // Create the view of startup, before the first render.
  const isMounted = useRef(false)
  if (!isMounted.current) {
    console.log('Registering view definition.')
    installView()
  }
  useEffect(() => {
    isMounted.current = true
  }, [])

  const [viewJson, setViewJson] = useState(props.viewJson)
  useEffect(() => {
    if (!isEqual(viewJson, props.viewJson)) {
      setViewJson(props.viewJson)
    }
  }, [props.viewJson])

  // Reinstall the view whenever the definition changes.
  useEffect(() => {
    console.log('View definition changed, re-registering the view definition.')
    installView()
    viewRef.current?.resetInstance()
    return () => {
      uninstallView()
    }
  }, [viewJson])

  const viewRef = useRef<JoinableView>(null)

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
