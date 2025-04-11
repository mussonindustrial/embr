package com.mussonindustrial.embr.perspective.gateway.component

import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegateFactory
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.perspective.common.component.PerspectiveComponent

interface GatewayComponent : PerspectiveComponent {
    val delegateFactory: ComponentModelDelegateFactory?
}

class GatewayComponentWrapper(
    private val component: PerspectiveComponent,
    override val delegateFactory: ComponentModelDelegateFactory? = null,
) : PerspectiveComponent by component, GatewayComponent {}

fun PerspectiveComponent.asGatewayComponent(
    delegateFactory: ComponentModelDelegateFactory? = null
): GatewayComponent {
    return GatewayComponentWrapper(this, delegateFactory)
}

fun PerspectiveContext.registerComponent(component: GatewayComponent) {
    this.componentRegistry.registerComponent(component.descriptor)
    component.delegateFactory?.let {
        this.componentModelDelegateRegistry.register(component.id, it)
    }
}

fun PerspectiveContext.removeComponent(component: GatewayComponent) {
    this.componentRegistry.removeComponent(component.id)
    this.componentModelDelegateRegistry.remove(component.id)
}
