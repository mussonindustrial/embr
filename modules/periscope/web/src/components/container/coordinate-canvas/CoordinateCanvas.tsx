import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentProps,
  ComponentStore,
  ComponentStoreDelegate,
  CoordinateUtils,
  JsObject,
  PComponent,
  PlainObject,
  PropertyTree,
  SizeObject,
} from '@inductiveautomation/perspective-client'
import { CoordinateContainer } from '@inductiveautomation/perspective-components'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'
import { cloneDeep } from 'lodash'
import React, { createRef, ReactElement, RefObject } from 'react'
import { CoordinateCanvasProps } from './types'
import { PanZoomSheet, PanZoomSheetRef } from './PanZoomSheet'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from '@embr-js/perspective-client'

const COMPONENT_TYPE = 'embr.periscope.container.coordinate-canvas'

function extractChildren(element: ReactElement) {
  const children = element.props.children
  delete element.props.children

  return {
    element,
    children,
  }
}

export class CoordinateCanvasComponent extends CoordinateContainer {
  readonly sheet: RefObject<PanZoomSheetRef>

  constructor(props: ComponentProps<CoordinateCanvasProps>) {
    super(props)
    this.sheet = createRef<PanZoomSheetRef>()
    if (props.store.delegate) {
      const delegate = props.store.delegate as CoordinateCanvasComponentDelegate
      delegate.attachComponent(this)
    }
  }

  findChild(name: string): ComponentStore | undefined {
    return this.props.store.children.find(
      (store) => store.meta.readString('name', '') == name
    )
  }

  getChildSize(name: string): DOMRect | undefined {
    const child = this.findChild(name)
    console.log('store', this.props.store)
    if (child && child.element && this.props.store.element) {
      const componentBounds = this.props.store.element.getBoundingClientRect()
      const childBounds = child.element.getBoundingClientRect()

      console.log('component', componentBounds)
      console.log('child', childBounds)
      return {
        x: componentBounds.x - childBounds.x,
        y: componentBounds.y - childBounds.y,
        left: componentBounds.left - childBounds.left,
        right: componentBounds.right - childBounds.right,
        top: componentBounds.top - childBounds.top,
        bottom: componentBounds.bottom - childBounds.bottom,
        height: childBounds.height,
        width: childBounds.width,
        toJSON: childBounds.toJSON,
      }
    }
    return undefined
  }

  fitToChild(name: string) {
    const size = this.getChildSize(name)
    if (size && this.sheet?.current !== null) {
      console.info(`Fitting to child ${name}: size[${size?.x}, ${size?.y}]`)
      this.sheet.current.panTo(
        {
          x: size.x,
          y: size.y,
        },
        {
          duration: 100,
        }
      )
    }
  }

  override render(): ReactElement {
    const props = this.props.props as CoordinateCanvasProps

    const { children, element } = extractChildren(this.renderContainer())

    return (
      <PanZoomSheet
        ref={this.sheet}
        initial={props.position}
        containerProps={element.props}
        gestures={props.settings.gestures}
        springs={props.settings.springs}
      >
        {children}
      </PanZoomSheet>
    )
  }
}

export class CoordinateCanvasComponentDelegate extends ComponentStoreDelegate {
  private proxyProps: JsObject = {}
  private jsProxy = new ComponentDelegateJavaScriptProxy(this, this.proxyProps)

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

    if (this.jsProxy.handles(eventName)) {
      this.proxyProps.sheet = this.coordinateCanvas.sheet.current
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
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
  focusRootOnly = true
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
