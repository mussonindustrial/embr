import React, { memo } from 'react'
import {
  ClientStore,
  ComponentMeta,
  ComponentProps,
  JsObject,
  PageStore,
  PComponent,
  PlainObject,
  PropertyTree,
  SizeObject,
  StyleObject,
  View,
} from '@inductiveautomation/perspective-client'

import {  formatStyleNames, mergeStyles, resolve } from '../../../util';

const COMPONENT_TYPE = 'embr.periscope.embedding.advanced-flex-repeater'

type AdvancedFlexRepeaterSettings = {
  direction: 'row' | 'row-reverse' | 'column' | 'column-reverse'
  wrap: 'nowrap' | 'wrap' | 'wrap-reverse'
  justify: 'flex-start' | 'flex-end' | 'center' | 'space-between' | 'space-around' | 'space-evenly'
  alignItems: 'flex-start' | 'flex-end'| 'center' | 'baseline' | 'stretch'
  alignContent: 'flex-start' | 'flex-end'| 'center' | 'space-between' | 'space-around' | 'stretch'
}

type AdvancedFlexRepeaterProps = {
  instances: EmbeddedViewProps[]
  instanceCommon: EmbeddedViewProps
  settings?: AdvancedFlexRepeaterSettings
  style?: StyleObject
}

type EmbeddedViewProps = {
  viewPath: string
  viewParams: JsObject
  viewStyle: StyleObject
  useDefaultHeight: boolean
  useDefaultMinHeight: boolean
  useDefaultMinWidth: boolean
  useDefaultWidth: boolean
}
    
type EmbeddedSlideViewProps = {
  store: ClientStore
  mountPath: string
  view: EmbeddedViewProps
}

function getChildMountPath(props: ComponentProps<PlainObject>, childIndex: any) {
  return `${props.store.viewMountPath}$${props.store.addressPathString}[${childIndex}]`
}

function resolveViewProps(props: AdvancedFlexRepeaterProps, index: number): EmbeddedViewProps {
  const view = props.instances[index]

  return {
      viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
      viewParams: {
        ...props.instanceCommon.viewParams,
        ...view.viewParams,
        index: index
      },
      viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
      useDefaultHeight: resolve([view.useDefaultHeight, props.instanceCommon.useDefaultHeight]),
      useDefaultMinHeight: resolve([view.useDefaultMinHeight, props.instanceCommon.useDefaultMinHeight]),
      useDefaultMinWidth: resolve([view.useDefaultMinWidth, props.instanceCommon.useDefaultMinWidth]),
      useDefaultWidth: resolve([view.useDefaultWidth, props.instanceCommon.useDefaultWidth])
  }
}

const EmbeddedView = memo(({ store, mountPath, view }: EmbeddedSlideViewProps) => {
  return (
    <>        
      <View
        key={PageStore.instanceKeyFor(view.viewPath, mountPath)}
        store={store}
        mountPath={mountPath}
        resourcePath={view.viewPath}
        useDefaultHeight={view.useDefaultHeight}
        useDefaultMinHeight={view.useDefaultMinHeight}
        useDefaultMinWidth={view.useDefaultMinWidth}
        useDefaultWidth={view.useDefaultWidth}
        params={view.viewParams}
        rootStyle={{
          ...view.viewStyle,
          classes: formatStyleNames(view.viewStyle.classes)
        }}
      />
    </>
  )
})

export function AdvancedFlexRepeaterComponent(props: ComponentProps<AdvancedFlexRepeaterProps>) {    

    props.store.view.custom

    const containerProps = props.emit()
    containerProps.style = {
      ...containerProps.style,
      display: 'flex',
      flexDirection: props.props.settings?.direction,
      flexWrap: props.props.settings?.wrap,
      justifyContent: props.props.settings?.justify,
      alignItems: props.props.settings?.alignItems,
      alignContent: props.props.settings?.alignContent
    }

    return (
      <div { ...containerProps } >
       { props.props.instances.map((_, index) => {
            const mountPath = getChildMountPath(props, index)
            const viewProps = resolveViewProps(props.props, index)
            viewProps.viewParams.index = index

            return (
                <EmbeddedView 
                  store={props.store.view.page.parent} 
                  view={viewProps}
                  mountPath={mountPath}
                />
            )
          })}
      </div>
    )
}

export class AdvancedFlexRepeaterComponentMeta implements ComponentMeta {
  getComponentType(): string {
    return COMPONENT_TYPE
  }

  getDefaultSize(): SizeObject {
    return {
      width: 300,
      height: 300,
    }
  }

  getPropsReducer(tree: PropertyTree): AdvancedFlexRepeaterProps {
    return {
      instances: tree.read('instances', []),
      instanceCommon: tree.read('instanceCommon', {}),
      settings: tree.read('settings', {}),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return AdvancedFlexRepeaterComponent as PComponent
  }
}
