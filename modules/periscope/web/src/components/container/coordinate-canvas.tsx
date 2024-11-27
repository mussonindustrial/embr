import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentStoreDelegate,
  CoordinateUtils,
  JsObject,
  PComponent,
  PropertyTree,
  SizeObject,
} from '@inductiveautomation/perspective-client'
import { CoordinateContainer } from '@inductiveautomation/perspective-components'
import { InteractionRegistry } from '@inductiveautomation/perspective-designer'
import { useDrag } from '@use-gesture/react'
import { cloneDeep } from 'lodash'
import React, { ReactElement, useState } from 'react'

const COMPONENT_TYPE = 'embr.periscope.container.coordinate-canvas'

type CoordinateCanvasProps = {
  mode: CoordinateUtils.LayoutMode,
  aspectRatio: string,
  pipes: []
}

if (window.hasOwnProperty("PerspectiveDesigner")) {
  const coordinateId = 'ia.container.coord'
  const coordinateDelegate = InteractionRegistry.getContainerDelegate('ia.container.coord')
  if (coordinateDelegate == null) {
    console.warn(`Could not find ContainerDelegate for ${coordinateId}`)
  } else {

    const coordinateCanvasDelegate = cloneDeep(coordinateDelegate)
    coordinateCanvasDelegate.type = COMPONENT_TYPE
    InteractionRegistry.registerInteractionDelegates(coordinateCanvasDelegate)
  }
}

type WrapperProps = {
  position: JsObject
  renderContainer: () => ReactElement
}

function CoordinateWrapper(props: WrapperProps) {

  const coordinateContents = props.renderContainer() as JSX.Element

  const children = coordinateContents.props.children as JSX.Element[]
  const containerProps = coordinateContents.props
  containerProps['children'] = []

  const [{ left: lastLeft, top: lastTop }, setLastPosition] = useState({ left: 0, top: 0 })
  const [{ left: currentLeft, top: currentTop }, setPosition] = useState({ left: 0, top: 0 })

  // Set the drag hook and define component movement based on gesture data
  const bind = useDrag(({ down, movement: [mLeft, mTop] }) => {
    // setPosition({ x: down ? mx : 0, y: down ? my : 0 })
    

    if (!down) {
      setLastPosition({left: currentLeft, top: currentTop})
    } else {
      setPosition({ left: lastLeft + mLeft, top: lastTop + mTop })
    }
  })

  return (
    <div {...containerProps}>
      <div {...bind()} className='coordinate-wrapper' style={{position: 'absolute', left: currentLeft, top: currentTop}}>
        {children}
      </div>
    </div>
  )
}

export class CoordinateCanvasComponent extends CoordinateContainer {
  override render(): JSX.Element {

    return <CoordinateWrapper
      position={this.props.position}
      renderContainer={this.renderContainer as () => ReactElement}
    />
  }
}

export class CoordinateCanvasComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class CoordinateCanvasComponentMeta implements ComponentMeta {

  isContainer = true;
  focusRootOnly = false;
  pipingEnabled = true;

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
      mode: tree.readString("mode", CoordinateUtils.LayoutMode.FIXED),
      aspectRatio: tree.readString("aspectRatio", ""),
      pipes: tree.readArray("pipes")
    } as never
  }

  getViewComponent(): PComponent {
    return CoordinateCanvasComponent as unknown as PComponent
  }
}
