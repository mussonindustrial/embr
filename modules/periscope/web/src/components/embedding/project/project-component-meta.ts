import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentStoreDelegate,
  PComponent,
  PropertyTree,
} from '@inductiveautomation/perspective-client'
import type { SizeObject } from '@inductiveautomation/perspective-common'

import { toComponentId, ProjectComponent, Values } from '@/api'
import {
  createProjectComponent,
  ProjectComponentDelegate,
} from './project-component'

export function findProjectComponent(type: string): ProjectComponent | null {
  return Embr?.clientResources?.components.get(toComponentId(type)) ?? null
}

export class ProjectComponentMeta implements ComponentMeta {
  private readonly type: string
  private readonly component: PComponent

  constructor(type: string) {
    this.type = type
    this.component = createProjectComponent(toComponentId(type)) as PComponent
  }

  getComponentType(): string {
    return this.type
  }

  get isContainer(): boolean {
    return findProjectComponent(this.type)?.periscope.position != null
  }

  get isDeepSelectable(): boolean {
    return this.isContainer
  }

  private get defaultSize() {
    return findProjectComponent(this.type)?.periscope.defaultSize
  }

  getRootDefaultSize(): Values {
    return this.defaultSize ?? { width: 800, height: 600 }
  }

  getDefaultSize(): SizeObject {
    return this.defaultSize ?? { width: 300, height: 300 }
  }

  createDelegate(component: AbstractUIElementStore): ComponentStoreDelegate {
    return new ProjectComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): Values {
    return { ...tree.read(), style: tree.readStyle('style') }
  }

  getViewComponent(): PComponent {
    return this.component
  }
}
