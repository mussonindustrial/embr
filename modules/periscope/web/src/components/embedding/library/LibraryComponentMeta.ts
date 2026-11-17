import {
  AbstractUIElementStore,
  ComponentMeta,
  ComponentStoreDelegate,
  PComponent,
  PropertyTree,
} from '@inductiveautomation/perspective-client'
import type { SizeObject } from '@inductiveautomation/perspective-common'

import { COMPONENT_TYPE_PREFIX } from '@/api/periscopeApi'
import { ReactComponentDelegate } from '../react'
import { createViewComponent } from './LibraryViewComponent'
import { findLibraryComponent, Values } from './shared'

export class LibraryComponentMeta implements ComponentMeta {
  private readonly component: PComponent

  constructor(private readonly type: string) {
    const id = type.slice(COMPONENT_TYPE_PREFIX.length)
    this.component = createViewComponent(id) as PComponent
  }

  getComponentType(): string {
    return this.type
  }

  get isContainer(): boolean {
    return findLibraryComponent(this.type)?.periscope.position != null
  }

  get isDeepSelectable(): boolean {
    return this.isContainer
  }

  getRootDefaultSize(): Values {
    const size = findLibraryComponent(this.type)?.periscope.defaultSize
    return size ?? { width: 800, height: 600 }
  }

  getDefaultSize(): SizeObject {
    const size = findLibraryComponent(this.type)?.periscope.defaultSize
    return size ?? { width: 300, height: 300 }
  }

  createDelegate(component: AbstractUIElementStore): ComponentStoreDelegate {
    return new ReactComponentDelegate(component)
  }

  getPropsReducer(tree: PropertyTree): Values {
    return { ...tree.read(), style: tree.readStyle('style') }
  }

  getViewComponent(): PComponent {
    return this.component
  }
}
