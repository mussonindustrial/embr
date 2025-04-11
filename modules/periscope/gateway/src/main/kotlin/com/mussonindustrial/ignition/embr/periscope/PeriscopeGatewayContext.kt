package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.embr.perspective.gateway.component.asGatewayComponent
import com.mussonindustrial.embr.perspective.gateway.component.registerComponent
import com.mussonindustrial.embr.perspective.gateway.component.removeComponent
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import java.util.WeakHashMap

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val perspectiveContext: PerspectiveContext
    private val components =
        listOf(
            EmbeddedView.asGatewayComponent { EmbeddedViewModelDelegate(it) },
            FlexRepeater.asGatewayComponent { FlexRepeaterModelDelegate(it) },
            JsonView.asGatewayComponent { JsonViewModelDelegate(it) },
            Portal.asGatewayComponent(),
            Swiper.asGatewayComponent(),
        )

    init {
        instance = this
        perspectiveContext = PerspectiveContext.get(context)
    }

    private val viewLoaders = WeakHashMap<PageModel, ViewLoader>()

    fun getViewLoader(pageModel: PageModel): ViewLoader {
        viewLoaders[pageModel]?.apply {
            return this
        }

        val newViewLoader = ViewLoader(pageModel)
        viewLoaders[pageModel] = newViewLoader
        return newViewLoader
    }

    fun registerComponents() {
        components.forEach { perspectiveContext.registerComponent(it) }
    }

    fun removeComponents() {
        components.forEach { perspectiveContext.removeComponent(it) }
    }

    fun injectResources() {
        perspectiveContext.componentRegistry.addResourcesTo(
            PeriscopeComponents.REQUIRED_RESOURCES
        ) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }
    }

    fun removeResources() {
        perspectiveContext.componentRegistry.removeResourcesFrom(
            PeriscopeComponents.REQUIRED_RESOURCES
        ) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }
    }
}
