import {
  AbstractUIElementStore,
  ComponentDefinition,
  ComponentInstanceDef,
  ComponentMeta,
  ComponentStoreDelegate,
  JsObject,
  PComponent,
  PropertyTree,
  SizeObject,
} from '@inductiveautomation/perspective-client'
import {
  ComponentDesignDelegate,
  ContainerDesignDelegate,
  DesignerComponentStore,
  PreferredLocation,
  SelectionStore,
} from '@inductiveautomation/perspective-designer'
import {
  ComponentDelegateJavaScriptProxy,
  JavaScriptRunEvent,
} from '@embr-js/perspective-client'

export type MuijoyComponent = {
  meta: ComponentMeta
  designDelegate?: ComponentDesignDelegate
}

export class MuiJoyComponentMeta implements ComponentMeta {
  type: string
  component: PComponent
  isDeepSelectable = true as const

  constructor(type: string, component: PComponent) {
    this.type = type
    this.component = component
  }

  getComponentType(): string {
    return this.type
  }

  getDefaultSize(): SizeObject {
    return {
      width: 200,
      height: 200,
    }
  }

  getViewComponent(): PComponent {
    return this.component
  }

  getPropsReducer(tree: PropertyTree) {
    const fullTree = tree.readObject('')
    delete fullTree['style']
    return fullTree
  }

  createDelegate(
    component: AbstractUIElementStore
  ): ComponentStoreDelegate | undefined {
    return new MuiJoyComponentStoreDelegate(component)
  }
}

export class MuiJoyComponentStoreDelegate extends ComponentStoreDelegate {
  private jsProxy = new ComponentDelegateJavaScriptProxy(this)

  setRef(ref?: object) {
    this.jsProxy.setRef(ref)
  }

  handleEvent(eventName: string, eventObject: JsObject): void {
    if (this.jsProxy.handles(eventName)) {
      this.jsProxy.handleEvent(eventObject as JavaScriptRunEvent)
    }
  }
}

export class MuiJoyDesignDelegate implements ContainerDesignDelegate {
  type: string
  isContainer = true as const

  constructor(type: string) {
    this.type = type
  }

  addNewComponents(
    components: ComponentDefinition[],
    _selection: SelectionStore,
    dropContainer: DesignerComponentStore,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    _preferredLocation?: PreferredLocation | undefined
  ): ComponentInstanceDef[] {
    return components.map((component) => {
      return {
        component,
        addressPath: dropContainer._addComponent(component),
      }
    })
  }
}
