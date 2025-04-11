package com.mussonindustrial.embr.perspective.designer.component

import com.inductiveautomation.perspective.designer.api.ComponentDesignDelegate
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent

interface DesignerComponent : PerspectiveComponent {
    val delegate: ComponentDesignDelegate?
}

class DesignerComponentWrapper(
    private val component: PerspectiveComponent,
    override val delegate: ComponentDesignDelegate? = null,
) : PerspectiveComponent by component, DesignerComponent

fun PerspectiveComponent.asDesignerComponent(
    delegate: ComponentDesignDelegate? = null
): DesignerComponent {
    return DesignerComponentWrapper(this, delegate)
}

fun PerspectiveDesignerInterface.registerComponent(component: DesignerComponent) {
    this.designerComponentRegistry.registerComponent(component.descriptor.asDesignerDescriptor())
    component.delegate?.let { this.componentDesignDelegateRegistry.register(component.id, it) }
}

fun PerspectiveDesignerInterface.removeComponent(component: DesignerComponent) {
    this.designerComponentRegistry.removeComponent(component.id)
    this.componentDesignDelegateRegistry.remove(component.id)
}
