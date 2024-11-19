import React from 'react'
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
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'
import { JoinableView } from '../../util/JoinableView'

const COMPONENT_TYPE = 'embr.periscope.embedding.view'

type EmbeddedViewProps = {
  viewPath: string
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

export function EmbeddedViewComponent({
  props,
  store,
  emit,
}: ComponentProps<EmbeddedViewProps>) {
  const mountPath = getChildMountPath(store)

  if (store.delegate == null) {
    console.warn(
      `No delegate found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate emit={emit} />
  }

  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <JoinableView
        key={PageStore.instanceKeyFor(props.viewPath, mountPath)}
        parent={store}
        store={store.view.page.parent}
        mountPath={mountPath}
        resourcePath={props.viewPath}
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

export class EmbeddedViewComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class EmbeddedViewComponentMeta implements ComponentMeta {
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
    return new EmbeddedViewComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): EmbeddedViewProps {
    return {
      viewPath: tree.readString('viewPath', ''),
      viewStyle: tree.readStyle('viewStyle'),
      useDefaultHeight: tree.readBoolean('useDefaultHeight'),
      useDefaultMinHeight: tree.readBoolean('useDefaultMinHeight'),
      useDefaultMinWidth: tree.readBoolean('useDefaultMinWidth'),
      useDefaultWidth: tree.readBoolean('useDefaultWidth'),
      style: tree.readStyle('style'),
    } as never
  }

  getViewComponent(): PComponent {
    return EmbeddedViewComponent as PComponent
  }
}
