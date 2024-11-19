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
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'

import {
  formatStyleNames,
  JoinableView,
  mergeStyles,
  resolve,
} from '../../util'

const COMPONENT_TYPE = 'embr.periscope.embedding.flex-repeater'

type FlexRepeaterSettings = {
  direction: 'row' | 'row-reverse' | 'column' | 'column-reverse'
  wrap: 'nowrap' | 'wrap' | 'wrap-reverse'
  justify:
    | 'flex-start'
    | 'flex-end'
    | 'center'
    | 'space-between'
    | 'space-around'
    | 'space-evenly'
  alignItems: 'flex-start' | 'flex-end' | 'center' | 'baseline' | 'stretch'
  alignContent:
    | 'flex-start'
    | 'flex-end'
    | 'center'
    | 'space-between'
    | 'space-around'
    | 'stretch'
}

type FlexPositionProps = {
  align: string
  basis: string | number
  grow: number
  shrink: number
}

type FlexRepeaterProps = {
  instances: EmbeddedViewProps[]
  instanceCommon: EmbeddedViewProps
  settings?: FlexRepeaterSettings
  style?: StyleObject
}

type EmbeddedViewProps = {
  key: string
  viewPath: string
  viewParams: JsObject
  viewStyle: StyleObject
  viewPosition: FlexPositionProps
  useDefaultHeight: boolean
  useDefaultMinHeight: boolean
  useDefaultMinWidth: boolean
  useDefaultWidth: boolean
}

function emitFlexPosition(props: FlexPositionProps): React.CSSProperties {
  ComponentStore
  return {
    alignSelf: props.align,
    flexBasis: props.basis,
    flexGrow: props.grow,
    flexShrink: props.shrink,
  }
}

function getChildMountPath(store: ComponentStore, key: string) {
  return `${store.viewMountPath}$${store.addressPathString}.${key}`
}

function resolveViewProps(
  props: FlexRepeaterProps,
  index: number
): EmbeddedViewProps {
  const view = props.instances[index]

  return {
    key: view.key && view.key !== '' ? view.key : index.toString(),
    viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
    viewParams: {},
    viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
    viewPosition: {
      ...props.instanceCommon.viewPosition,
      ...view.viewPosition,
    },
    useDefaultHeight: resolve([
      view.useDefaultHeight,
      props.instanceCommon.useDefaultHeight,
    ]),
    useDefaultMinHeight: resolve([
      view.useDefaultMinHeight,
      props.instanceCommon.useDefaultMinHeight,
    ]),
    useDefaultMinWidth: resolve([
      view.useDefaultMinWidth,
      props.instanceCommon.useDefaultMinWidth,
    ]),
    useDefaultWidth: resolve([
      view.useDefaultWidth,
      props.instanceCommon.useDefaultWidth,
    ]),
  }
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

type DelegateEmbeddedViewProps = {
  view: EmbeddedViewProps
  mountPath: string
  key: string
  store: ComponentStore
}
const DelegateEmbeddedView = memo(function DelegateEmbeddedView({
  view,
  mountPath,
  key,
  store,
}: DelegateEmbeddedViewProps) {
  if (store.delegate == null) {
    console.warn(
      `No delegate found for component ${COMPONENT_TYPE} at ${mountPath}`
    )
    return <MissingComponentDelegate key={key} emit={store.emitterFactory()} />
  }

  return (
    <JoinableView
      key={key}
      store={store.view.page.parent}
      mountPath={mountPath}
      resourcePath={view.viewPath}
      useDefaultHeight={view.useDefaultHeight}
      useDefaultMinHeight={view.useDefaultMinHeight}
      useDefaultMinWidth={view.useDefaultMinWidth}
      useDefaultWidth={view.useDefaultWidth}
      rootStyle={{
        ...emitFlexPosition(view.viewPosition),
        ...view.viewStyle,
        classes: formatStyleNames(view.viewStyle.classes),
      }}
      delegate={store.delegate}
    />
  )
})

export function FlexRepeaterComponent({
  props,
  store,
  emit,
}: ComponentProps<FlexRepeaterProps>) {
  const containerProps = emit({ classes: ['view-parent'] })
  containerProps.style = {
    ...containerProps.style,
    display: 'flex',
    flexDirection: props.settings?.direction,
    flexWrap: props.settings?.wrap,
    justifyContent: props.settings?.justify,
    alignItems: props.settings?.alignItems,
    alignContent: props.settings?.alignContent,
  }

  return (
    <div {...containerProps}>
      {props.instances.map((_, index) => {
        const view = resolveViewProps(props, index)
        const mountPath = getChildMountPath(store, view.key)
        const key = PageStore.instanceKeyFor(view.viewPath, mountPath)

        return (
          <DelegateEmbeddedView
            key={key}
            mountPath={mountPath}
            view={view}
            store={store}
          />
        )
      })}
    </div>
  )
}

export class FlexRepeaterComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class FlexRepeaterComponentMeta implements ComponentMeta {
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
    return new FlexRepeaterComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): FlexRepeaterProps {
    return {
      instances: tree.read('instances', []),
      instanceCommon: tree.read('instanceCommon', {}),
      settings: tree.read('settings', {}),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return FlexRepeaterComponent as PComponent
  }
}
