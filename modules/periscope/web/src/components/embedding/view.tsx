import React, { memo } from 'react'
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
  View,
  ViewProps,
  ViewStateDisplay,
  ViewStore,
} from '@inductiveautomation/perspective-client'

import { formatStyleNames } from '../../util'

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

type JoinableViewProps = ViewProps & {
  delegate: ComponentStoreDelegate
}

const JoinableView = memo(
  class extends View {
    delegate: ComponentStoreDelegate

    constructor(props: JoinableViewProps) {
      super(props)
      this.delegate = props.delegate
      this.installViewStore = this.installViewStore.bind(this)
    }

    override installViewStore(viewStore: ViewStore): void {
      super.installViewStore(joinOnStartup(viewStore, this.delegate))
    }
  }
)

function joinOnStartup(viewStore: ViewStore, delegate: ComponentStoreDelegate) {
  Reflect.defineProperty(viewStore, 'startup', {
    value: () => {
      const params = viewStore.running
        ? viewStore.params.readEncoded('', false)
        : viewStore.initialParams

      delegate.fireEvent('view-join', {
        resourcePath: viewStore.resourcePath,
        mountPath: viewStore.mountPath,
        birthDate: viewStore.birthDate,
        params,
      })
      viewStore.running = true
    },
  })
  return viewStore
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

  if (store.delegate == undefined) {
    console.warn(
      `No delegate found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate emit={emit} />
  }

  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <JoinableView
        key={PageStore.instanceKeyFor(props.viewPath, mountPath)}
        store={store.view.page.parent}
        mountPath={mountPath}
        resourcePath={props.viewPath}
        useDefaultHeight={props.useDefaultHeight}
        useDefaultMinHeight={props.useDefaultMinHeight}
        useDefaultMinWidth={props.useDefaultMinWidth}
        useDefaultWidth={props.useDefaultWidth}
        rootStyle={{
          ...props.viewStyle,
          classes: formatStyleNames(props.viewStyle.classes),
        }}
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        delegate={store.delegate!}
      />
    </div>
  )
}

export class EmbeddedViewComponentDelegate extends ComponentStoreDelegate {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
  handleEvent(_eventName: string, _eventObject: JsObject): void {}
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
      viewStyle: tree.read('viewStyle', {}),
      useDefaultHeight: tree.readBoolean('useDefaultHeight'),
      useDefaultMinHeight: tree.readBoolean('useDefaultMinHeight'),
      useDefaultMinWidth: tree.readBoolean('useDefaultMinWidth'),
      useDefaultWidth: tree.readBoolean('useDefaultWidth'),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return EmbeddedViewComponent as PComponent
  }
}
