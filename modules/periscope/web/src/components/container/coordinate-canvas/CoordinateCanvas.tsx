import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentStore,
  ComponentStoreDelegate,
  CoordinateUtils,
  PComponent,
  PlainObject,
  PropertyTree,
  SizeObject,
} from '@inductiveautomation/perspective-client'
import { CoordinateContainer } from '@inductiveautomation/perspective-components'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'
import { cloneDeep } from 'lodash'
import React, { ReactElement } from 'react'
import { SpringRef } from '@react-spring/web'
import { CoordinateCanvasProps, WrapperApiProps } from './types'
import { CoordinateContainerWrapper } from './CoordinateContainerWrapper'

const COMPONENT_TYPE = 'embr.periscope.container.coordinate-canvas'

export class CoordinateCanvasComponent extends CoordinateContainer {
  private api: SpringRef<WrapperApiProps> | undefined

  setApi(api: SpringRef<WrapperApiProps>) {
    this.api = api
  }

  findChild(name: string): ComponentStore | undefined {
    return this.props.store.children.find(
      (store) => store.meta.readString('name', '') == name
    )
  }

  getChildSize(name: string): DOMRect | undefined {
    const child = this.findChild(name)
    if (child && child.element) {
      return child.element.getBoundingClientRect()
    }
    return undefined
  }

  fitToChild(name: string) {
    const size = this.getChildSize(name)
    console.info(`Fitting to child ${name}: size[${size?.x}, ${size?.y}]`)
    if (size && this.api !== undefined) {
      this.api.start({
        x: size.x,
        y: size.y,
      })
    }
  }

  override render(): ReactElement {
    const props = this.props.props as CoordinateCanvasProps
    return (
      <CoordinateContainerWrapper
        setApi={this.setApi.bind(this)}
        settings={props.settings}
        position={props.position}
        wrapped={this.renderContainer as () => ReactElement}
      />
    )
  }
}

export class CoordinateCanvasComponentDelegate extends ComponentStoreDelegate {
  private coordinateCanvas: CoordinateCanvasComponent | undefined

  constructor(componentStore: AbstractUIElementStore) {
    console.log('CoordinateCanvasComponentDelegate constructing.')
    super(componentStore)
  }

  attachComponent(coordinateCanvas: CoordinateCanvasComponent) {
    this.coordinateCanvas = coordinateCanvas
  }

  handleEvent(eventName: string, eventObject: PlainObject): void {
    if (this.coordinateCanvas == undefined) {
      console.warn('Received an event, but coordinate canvas is missing.')
      return
    }

    if (eventName == 'fit-child') {
      const { name } = eventObject
      console.info(`Fitting to child ${name}`)
      this.coordinateCanvas.fitToChild(name)
    }
  }
}

export class CoordinateCanvasComponentMeta implements ComponentMeta {
  isContainer = true
  focusRootOnly = false
  pipingEnabled = true

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
    return new CoordinateCanvasComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): CoordinateCanvasProps {
    return {
      mode: tree.readString('mode', CoordinateUtils.LayoutMode.FIXED),
      aspectRatio: tree.readString('aspectRatio', ''),
      pipes: tree.readArray('pipes'),
      position: tree.read('position', { x: 0, y: 0 }),
      settings: tree.read('settings', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return CoordinateCanvasComponent as unknown as PComponent
  }
}

if (Object.prototype.hasOwnProperty.call(window, 'PerspectiveDesigner')) {
  const coordinateId = 'ia.container.coord'
  const coordinateDelegate =
    InteractionRegistry.getContainerDelegate(coordinateId)
  if (coordinateDelegate == null) {
    console.warn(`Could not find ContainerDelegate for ${coordinateId}`)
  } else {
    const coordinateCanvasDelegate = cloneDeep(coordinateDelegate)
    coordinateCanvasDelegate.type = COMPONENT_TYPE
    InteractionRegistry.registerInteractionDelegates(coordinateCanvasDelegate)
  }
}
