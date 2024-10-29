import React, { memo } from 'react'
import {
  AbstractUIElementStore,
  ClientStore,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  JsObject,
  OutputListener,
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
  key: string
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
  outputListener?: OutputListener
}

function getChildMountPath(props: ComponentProps<PlainObject>, key: string) {
  return `${props.store.viewMountPath}$${props.store.addressPathString}.${key}`
}

function resolveViewProps(props: AdvancedFlexRepeaterProps, index: number): EmbeddedViewProps {
  const view = props.instances[index]

  return {
      key: view.key && view.key !== '' ? view.key : index.toString(),
      viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
      viewParams: {
        ...props.instanceCommon.viewParams,
        ...view.viewParams
      },
      viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
      useDefaultHeight: resolve([view.useDefaultHeight, props.instanceCommon.useDefaultHeight]),
      useDefaultMinHeight: resolve([view.useDefaultMinHeight, props.instanceCommon.useDefaultMinHeight]),
      useDefaultMinWidth: resolve([view.useDefaultMinWidth, props.instanceCommon.useDefaultMinWidth]),
      useDefaultWidth: resolve([view.useDefaultWidth, props.instanceCommon.useDefaultWidth])
  }
}

const EmbeddedView = memo(({ store, mountPath, view, outputListener }: EmbeddedSlideViewProps) => {
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
        outputListener={outputListener}
        rootStyle={{
          ...view.viewStyle,
          classes: formatStyleNames(view.viewStyle.classes)
        }}
      />
    </>
  )
})

export function AdvancedFlexRepeaterComponent(props: ComponentProps<AdvancedFlexRepeaterProps>) {    

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
          const viewProps = resolveViewProps(props.props, index)
          const mountPath = getChildMountPath(props, viewProps.key)
          const outputListener = (outputName: string, outputValue: any): void => {
            props.store.props.write(`instances[${index}].viewParams.${outputName}`, outputValue)
          }

          return (
              <EmbeddedView 
                store={props.store.view.page.parent} 
                view={viewProps}
                mountPath={mountPath}
                key={viewProps.key}
                outputListener={outputListener}
              />
          )
        })}
      </div>
    )
}

export class AdvancedFlexRepeaterGatewayDelegate extends ComponentStoreDelegate {

  handleEvent(eventName: string, eventObject: JsObject): void {
    console.log(`event: ${eventName} = ${eventObject}`)
  }

  constructor(componentStore: AbstractUIElementStore) {
      super(componentStore);
  }
  
}

export class AdvancedFlexRepeaterComponentMeta implements ComponentMeta {
  getComponentType(): string {
    return COMPONENT_TYPE
  }

  createDelegate(component: AbstractUIElementStore): ComponentStoreDelegate | undefined {
      return new AdvancedFlexRepeaterGatewayDelegate(component)
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
