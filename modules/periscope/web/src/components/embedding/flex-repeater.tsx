import React, { memo } from 'react'
import {
  ComponentMeta,
  ComponentProps,
  ComponentStore,
  JsObject,
  PageStore,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
  View,
  ViewProps,
  ViewStore,
} from '@inductiveautomation/perspective-client'

import { formatStyleNames, mergeStyles, resolve } from '../../util';

const COMPONENT_TYPE = 'embr.periscope.embedding.flex-repeater'

type FlexRepeaterSettings = {
  direction: 'row' | 'row-reverse' | 'column' | 'column-reverse'
  wrap: 'nowrap' | 'wrap' | 'wrap-reverse'
  justify: 'flex-start' | 'flex-end' | 'center' | 'space-between' | 'space-around' | 'space-evenly'
  alignItems: 'flex-start' | 'flex-end'| 'center' | 'baseline' | 'stretch'
  alignContent: 'flex-start' | 'flex-end'| 'center' | 'space-between' | 'space-around' | 'stretch'
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
  return {
    alignSelf: props.align,
    flexBasis: props.basis,
    flexGrow: props.grow,
    flexShrink: props.shrink
  }
}

function getChildMountPath(store: ComponentStore, key: string) {
  return `${store.viewMountPath}$${store.addressPathString}.${key}`
}

function resolveViewProps(props: FlexRepeaterProps, index: number): EmbeddedViewProps {
  const view = props.instances[index]

  return {
      key: view.key && view.key !== '' ? view.key : index.toString(),
      viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
      viewParams: {
        index,
        ...props.instanceCommon.viewParams,
        ...view.viewParams
      },
      viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
      viewPosition: {
        ...props.instanceCommon.viewPosition,
        ...view.viewPosition
      },
      useDefaultHeight: resolve([view.useDefaultHeight, props.instanceCommon.useDefaultHeight]),
      useDefaultMinHeight: resolve([view.useDefaultMinHeight, props.instanceCommon.useDefaultMinHeight]),
      useDefaultMinWidth: resolve([view.useDefaultMinWidth, props.instanceCommon.useDefaultMinWidth]),
      useDefaultWidth: resolve([view.useDefaultWidth, props.instanceCommon.useDefaultWidth])
  }
}

const JoinableView = memo(class extends View {
  constructor(props: ViewProps) {
      super(props)
      this.installViewStore = this.installViewStore.bind(this);
  }

  override installViewStore(viewStore: ViewStore): void {
      super.installViewStore(joinOnStartup(viewStore))
  }
})

function joinOnStartup(viewStore: ViewStore) {
  Reflect.defineProperty(viewStore, 'startup', { value: () => {
      const channel = viewStore.page.parent.connection
      const params = viewStore.running ? viewStore.params.readEncoded('', false) : viewStore.initialParams

      channel.send('view-join', {
          resourcePath: viewStore.resourcePath,
          mountPath: viewStore.mountPath,
          birthDate: viewStore.birthDate,
          params
      })
      viewStore.running = true
  }})
  return viewStore
}

export function FlexRepeaterComponent({props, store, emit}: ComponentProps<FlexRepeaterProps>) {    

    const containerProps = emit({ classes: ['view-parent'] })
    containerProps.style = {
      ...containerProps.style,
      display: 'flex',
      flexDirection: props.settings?.direction,
      flexWrap: props.settings?.wrap,
      justifyContent: props.settings?.justify,
      alignItems: props.settings?.alignItems,
      alignContent: props.settings?.alignContent
    }    

    return (
      <div { ...containerProps } >
        { props.instances.map((_, index) => {
          const view = resolveViewProps(props, index)
          const mountPath = getChildMountPath(store, view.key)

          return (
              <JoinableView 
                key={PageStore.instanceKeyFor(view.viewPath, mountPath)}
                store={store.view.page.parent} 
                mountPath={mountPath}
                resourcePath={view.viewPath}
                useDefaultHeight={view.useDefaultHeight}
                useDefaultMinHeight={view.useDefaultMinHeight}
                useDefaultMinWidth={view.useDefaultMinWidth}
                useDefaultWidth={view.useDefaultWidth}
                params={view.viewParams}
                rootStyle={{
                  ...emitFlexPosition(view.viewPosition),
                  ...view.viewStyle,
                  classes: formatStyleNames(view.viewStyle.classes)
                }}
              />
          )
        })}
      </div>
    )
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
