package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.gateway.dataroutes.RouteAccessControl
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.gateway.GatewayHook
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.inductiveautomation.perspective.gateway.comm.Routes
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.embr.perspective.gateway.component.JavaScriptProxyableComponentModelDelegate
import com.mussonindustrial.embr.perspective.gateway.component.asGatewayComponent
import com.mussonindustrial.embr.perspective.gateway.component.registerComponent
import com.mussonindustrial.embr.perspective.gateway.component.removeComponent
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor
import com.mussonindustrial.embr.servlets.ClassLoaderResourceHandler
import com.mussonindustrial.embr.servlets.createDataServletRouteGroup
import com.mussonindustrial.embr.servlets.removeDataServletRouteGroup
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.handlers.ClientResourceHandler
import com.mussonindustrial.ignition.embr.periscope.handlers.ClientResourceManifestHandler
import com.mussonindustrial.ignition.embr.periscope.handlers.SystemModuleHandler
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceChangeListener
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import java.util.EnumSet
import java.util.WeakHashMap

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val clientResourceManager = ClientResourceManager()

    val perspectiveContext: GatewayHook.PerspectiveGatewayContext
    val sessionMonitor: PerspectiveSessionMonitor
    private val components =
        listOf(
            EmbeddedView.asGatewayComponent { EmbeddedViewModelDelegate(it) },
            FlexRepeater.asGatewayComponent { FlexRepeaterModelDelegate(it) },
            JsonView.asGatewayComponent { JsonViewModelDelegate(it) },
            Portal.asGatewayComponent(),
            Swiper.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
            React.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
        )

    private val clientResourceDefinitions =
        listOf(CssModuleResource.Descriptor, TypeScriptResource.Descriptor)

    init {
        instance = this
        perspectiveContext =
            PerspectiveContext.get(context) as GatewayHook.PerspectiveGatewayContext
        sessionMonitor = PerspectiveSessionMonitor(perspectiveContext)
    }

    private val projectLifecycles =
        listOf(ClientResourceChangeListener(sessionMonitor, context.projectManager))
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

    fun registerClientResourceDefinitions() {
        clientResourceDefinitions.forEach { clientResourceManager.register(it) }
    }

    fun startupProjectLifecycles() {
        projectLifecycles.forEach { it.startup() }
    }

    fun shutdownProjectLifecycles() {
        projectLifecycles.forEach { it.shutdown() }
    }

    fun registerServlets() {
        val routeGroup = context.webResourceManager.createDataServletRouteGroup("embr-periscope")
        ClassLoaderResourceHandler(this.javaClass.classLoader, "static", "/resources")
            .mount(routeGroup.newRoute("/resources/*"))
        ClientResourceManifestHandler(this)
            .mount(routeGroup.newRoute("/client-resource/:project_name/manifest.json"))
        SystemModuleHandler(this).mount(routeGroup.newRoute("/system-module/:hash/:module_name"))
        ClientResourceHandler(this)
            .mount(routeGroup.newRoute("/client-resource/:project_name/:hash/*"))
    }

    fun unregisterServlets() {
        context.webResourceManager.removeDataServletRouteGroup("embr-periscope")
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }

    fun requireSession(scopes: EnumSet<SessionScope>): RouteAccessControl {
        val internal = Routes::class.java.getPrivateMethod("requireSession", EnumSet::class.java)
        return internal.invoke(Routes::class.java, scopes) as RouteAccessControl
    }
}
