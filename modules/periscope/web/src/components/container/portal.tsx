import React, { useEffect, useMemo, useRef } from 'react'
import {
  AbstractUIElementStore,
  ComponentDefinition,
  ComponentInstanceDef,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  LayoutBuilder,
  layoutCallbackCreator,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
} from '@inductiveautomation/perspective-client'
import { transformProps } from '@embr-js/utils'
import { createPortal } from 'react-dom'
import {
  ComponentEvents,
  getScriptTransform,
  useComponentEvents,
  waitForClientStore,
} from '@embr-js/perspective-client'
import {
  ContainerDesignDelegate,
  DesignerComponentStore,
  InteractionRegistry,
  PreferredLocation,
  SelectionStore,
} from '@inductiveautomation/perspective-designer'

const COMPONENT_TYPE = 'embr.periscope.container.portal'

type PortalProps = {
  element: string | (() => Element)
  events: ComponentEvents
  style: StyleObject
}

export function PortalComponent(props: ComponentProps<PortalProps>) {
  const target = useRef<Element | null>(null)

  const transformedProps = useMemo(() => {
    return transformProps(props.props, [
      getScriptTransform(props, props.store),
    ]) as PortalProps
  }, [props.props])

  useEffect(() => {
    if (typeof transformedProps.element === 'function') {
      target.current = transformedProps.element()
    } else {
      target.current = document.getElementById(transformedProps.element)
    }
  }, [props.props.element])

  if (target.current == null) {
    return <div>No Target</div>
  }

  useComponentEvents(props.store, transformedProps.events, props)

  return createPortal(
    <div {...props.emit()}>
      {props.store.children && props.store.children.length === 0 && (
        <div>
          <p>Drop components here</p>
        </div>
      )}
      {/* Loop over all children */}
      {props.store.children.map((componentStore, index) => {
        const Component = componentStore.getComponent()
        const buildLayout: LayoutBuilder = () => ({})
        const layout = layoutCallbackCreator.forStyle(buildLayout)

        return <Component key={index} layout={layout} />
      })}
    </div>,
    target.current
  )
}

export class PortalComponentDelegate extends ComponentStoreDelegate {
  handleEvent(): void {
    return
  }
}

export class PortalComponentMeta implements ComponentMeta {
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
    return new PortalComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): PortalProps {
    return {
      element: tree.readString('element', ''),
      style: tree.readStyle('style'),
    } as never
  }

  getViewComponent(): PComponent {
    return PortalComponent as PComponent
  }
}

class PortalComponentDesignDelegate implements ContainerDesignDelegate {
  type = COMPONENT_TYPE
  isContainer = true as const

  addNewComponents(
    compDefs: ComponentDefinition[],
    _selection: SelectionStore,
    dropContainer: DesignerComponentStore,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    _preferredLocation?: PreferredLocation | undefined
  ): ComponentInstanceDef[] {
    return compDefs.map((compDef) => {
      return {
        component: compDef,
        addressPath: dropContainer._addComponent(compDef),
      }
    })
  }
}

waitForClientStore((clientStore) => {
  if (clientStore.isDesigner) {
    InteractionRegistry.registerInteractionDelegates(
      new PortalComponentDesignDelegate()
    )
  }
})
