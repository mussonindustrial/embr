package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.project.Project
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
import com.mussonindustrial.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.periscope.component.LibraryComponentDefaults
import com.mussonindustrial.ignition.embr.periscope.component.LibraryComponentTypes
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.handlers.AssetHandler
import com.mussonindustrial.ignition.embr.periscope.js.modules.GlobalModule
import com.mussonindustrial.ignition.embr.periscope.js.modules.SystemModule
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceChangeListener
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManifests
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentLibraryResource
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.utils.getClientResourceHash
import java.security.MessageDigest
import java.util.EnumSet
import java.util.WeakHashMap
import org.apache.commons.codec.binary.Hex

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val servletManager = ModuleServletManager(context.webResourceManager, "/embr/periscope")
    val clientResourceManager = ClientResourceManager()
    val clientResourceManifests = ClientResourceManifests(clientResourceManager)

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
        listOf(
            CssModuleResource.Descriptor,
            TypeScriptResource.Descriptor,
            ComponentLibraryResource.Descriptor,
        )

    init {
        instance = this
        perspectiveContext =
            PerspectiveContext.get(context) as GatewayHook.PerspectiveGatewayContext
        sessionMonitor = PerspectiveSessionMonitor(perspectiveContext)
    }

    val componentDefaults = LibraryComponentDefaults(perspectiveContext)
    val assets by lazy { AssetHandler(this) }

    private val runtimeHash by lazy {
        val version = moduleManager.getModule(Meta.MODULE_ID)?.info?.version?.toString()
        val modules = (SystemModule.all + GlobalModule.all).map { it.emit() }
        val digest = MessageDigest.getInstance("SHA-256")
        (listOfNotNull(version) + assets.hashes + modules).forEach {
            digest.update(it.toByteArray())
            digest.update(0)
        }
        Hex.encodeHexString(digest.digest())
    }

    fun clientResourceHash(project: Project) =
        project.getClientResourceHash(runtimeHash)

    private val libraryComponentTypes =
        LibraryComponentTypes(perspectiveContext, componentDefaults, context.projectManager)
    private val projectLifecycles =
        listOf(
            ClientResourceChangeListener(sessionMonitor, context.projectManager),
            libraryComponentTypes,
        )
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

    fun versionAssets() {
        assets.version(PeriscopeComponents.REQUIRED_RESOURCES)
    }

    fun injectResources() {
        perspectiveContext.componentRegistry.addResourcesTo(
            PeriscopeComponents.REQUIRED_RESOURCES,
        ) {
            it.moduleId() == PerspectiveModule.MODULE_ID
        }
    }

    fun removeResources() {
        perspectiveContext.componentRegistry.removeResourcesFrom(
            PeriscopeComponents.REQUIRED_RESOURCES,
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

    fun startupComponentDefaults() {
        componentDefaults.startup()
    }

    fun shutdownComponentDefaults() {
        componentDefaults.shutdown()
    }

    fun removeLibraryComponents() {
        libraryComponentTypes.clear()
    }

    fun removeServlets() {
        servletManager.removeAllServlets()
    }

    override fun getTelemetryManager(): TelemetryManager? {
        return super.getTelemetryManager()
    }

    override fun getDiagnosticsManager(): DiagnosticsManager? {
        return super.getDiagnosticsManager()
    }

    val anySession: RouteAccessControl by lazy {
        val requireSession =
            Routes::class.java.getPrivateMethod("requireSession", EnumSet::class.java)
        val allScopes = EnumSet.allOf(SessionScope::class.java)
        requireSession.invoke(Routes::class.java, allScopes) as RouteAccessControl
    }
}
