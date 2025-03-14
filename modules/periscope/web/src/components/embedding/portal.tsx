import React, { useMemo, useRef } from 'react'
import {
  AbstractUIElementStore,
  ComponentDefinition,
  ComponentInstanceDef,
  ComponentMeta,
  ComponentProps,
  ComponentStoreDelegate,
  Emitter,
  layoutCallbackCreator,
  PComponent,
  PropertyTree,
  SizeObject,
  StyleObject,
  ViewStateDisplay,
} from '@inductiveautomation/perspective-client'
import { transformProps } from '@embr-js/utils'
import { createPortal } from 'react-dom'
import {
  ComponentEvents,
  ComponentLifecycleEvents,
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
import useRefLifecycleEvents from '@embr-js/perspective-client/src/hooks/useRefLifecycleEvents'

const COMPONENT_TYPE = 'embr.periscope.embedding.portal'

type PortalProps = {
  element: string | ((element: Element | void) => Element)
  events: ComponentEvents & {
    target: {
      lifecycle: ComponentLifecycleEvents
    }
  }
  style: StyleObject
}

function MissingTarget({ emit }: { emit: Emitter }) {
  return (
    <div {...emit({ classes: ['view-parent'] })}>
      <ViewStateDisplay
        primaryMessage="Portal Target Not Found"
        secondaryMessage={`Configured element does not exist.`}
        icon={
          <svg className="view-state-icon">
            <use xlinkHref="/res/perspective/icons/material-icons.svg#warning" />
          </svg>
        }
      />
    </div>
  )
}

export function PortalComponent(props: ComponentProps<PortalProps>) {
  const target = useRef<Element | null>()

  const transformedProps = useMemo(() => {
    return transformProps(props.props, [
      getScriptTransform(props, props.store),
    ]) as PortalProps
  }, [props.props])

  if (typeof transformedProps.element === 'function') {
    target.current = transformedProps.element(props.store.element)
  } else {
    target.current = document.getElementById(transformedProps.element)
  }

  useComponentEvents(props.store, transformedProps.events, target.current)
  useRefLifecycleEvents(
    transformedProps.events?.target?.lifecycle ?? {},
    target.current
  )

  if (transformedProps.element === '') {
    return <></>
  }

  if (target.current == null) {
    return <MissingTarget emit={props.emit} />
  }

  return createPortal(
    <>
      {props.store.children.map((componentStore, index) => {
        const Component = componentStore.getComponent()
        const layout = layoutCallbackCreator.forStyle(() => ({}))
        return <Component key={index} layout={layout} />
      })}
    </>,
    target.current,
    props.store.addressPathString
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
      events: tree.readObject('events', {}),
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
