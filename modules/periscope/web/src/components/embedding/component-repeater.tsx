import React, { useEffect, useState } from 'react'
import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStore,
  ComponentStoreDelegate,
  Emitter,
  JsObject,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'


const COMPONENT_TYPE = 'embr.periscope.embedding.component-repeater'

type ComponentRepeaterSettings = {
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

// type ComponentPositionProps = {
//   align: string
//   basis: string | number
//   grow: number
//   shrink: number
// }

type ComponentRepeaterProps = {
  instances: EmbeddedComponentProps[]
  settings?: ComponentRepeaterSettings
  style?: StyleObject
}

type EmbeddedComponentProps = {
  version: number,
  type: string
  props: JsObject
}

function getAddressPath(store: ComponentStore, index: number) {
  const addressPath = Object.assign([], store.addressPath)
  addressPath.push(index)
  return addressPath
}

function MissingComponent({ emit }: { emit: Emitter }) {
  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <ViewStateDisplay
        primaryMessage="Component Failed to Load"
        secondaryMessage={`No component was found`}
        icon={
          <svg className="view-state-icon">
            <use xlinkHref="/res/perspective/icons/material-icons.svg#warning" />
          </svg>
        }
      />
    </div>
  )
}

type TestProps = {
  parent: ComponentStore,
  index: number,
  instance: EmbeddedComponentProps
}
export function EmbeddedComponent( {parent, index, instance} : TestProps) {

  const [componentStore, setComponentStore] = useState<ComponentStore>()

  useEffect(() => {
    const addressPath = getAddressPath(parent, index)
    const newComponentStore = parent.view._newComponentStore(instance, parent, addressPath)

    newComponentStore.props.subscribe((tree) => {
      parent.props.write(`instances[${index}].props`, tree.toPlainObject())
    })

    parent.props.subscribe((tree) => {
      newComponentStore.props.write('', tree.read(`instances[${index}].props`))
    })

    parent.children[index] = newComponentStore
    setComponentStore(newComponentStore)
  }, [parent, index])

  if (componentStore == undefined) {
    return <MissingComponent emit={parent.emitterFactory()}></MissingComponent>
  }

  parent.childComponents[index] = componentStore
  const PerspectiveComponent = componentStore.getComponent()
  return <PerspectiveComponent/>
}

export function ComponentRepeaterComponent({
  props,
  store,
  emit,
}: ComponentProps<ComponentRepeaterProps>) {
  const containerProps = emit({ classes: ['view-parent'] })
  containerProps.style = {
    ...containerProps?.style,
    display: 'flex',
    flexDirection: props.settings?.direction,
    flexWrap: props.settings?.wrap,
    justifyContent: props.settings?.justify,
    alignItems: props.settings?.alignItems,
    alignContent: props.settings?.alignContent,
  }
  
  return (
    <div {...containerProps}>
      {props.instances.map((instance, index) => {

        return <EmbeddedComponent
          key={index}
          index={index}
          instance={instance}
          parent={store}
        />
      })}
    </div>
  )
}

export class ComponentRepeaterComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class ComponentRepeaterComponentMeta implements ComponentMeta {
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
    return new ComponentRepeaterComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): ComponentRepeaterProps {
    return {
      instances: tree.read('instances', []),
      settings: tree.read('settings', {}),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return ComponentRepeaterComponent as PComponent
  }
}
