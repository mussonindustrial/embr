package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.project.Project
import com.inductiveautomation.ignition.gateway.model.DiagnosticsManager
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.model.TelemetryManager
import com.inductiveautomation.perspective.common.PerspectiveModule
import com.inductiveautomation.perspective.gateway.GatewayHook
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.mussonindustrial.embr.common.crypto.sha256Delimited
import com.mussonindustrial.embr.common.crypto.toHex
import com.mussonindustrial.embr.gateway.EmbrGatewayContext
import com.mussonindustrial.embr.gateway.EmbrGatewayContextImpl
import com.mussonindustrial.embr.perspective.common.component.addResourcesTo
import com.mussonindustrial.embr.perspective.common.component.removeResourcesFrom
import com.mussonindustrial.embr.perspective.gateway.component.JavaScriptProxyableComponentModelDelegate
import com.mussonindustrial.embr.perspective.gateway.component.asGatewayComponent
import com.mussonindustrial.embr.perspective.gateway.component.registerComponent
import com.mussonindustrial.embr.perspective.gateway.component.removeComponent
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader
import com.mussonindustrial.embr.perspective.gateway.routes.AssetHandler
import com.mussonindustrial.embr.perspective.gateway.session.PerspectiveSessionMonitor
import com.mussonindustrial.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDefaultsManager
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentDelegateScriptManager
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentModelDelegate
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentRegistry
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.js.modules.GlobalModule
import com.mussonindustrial.ignition.embr.periscope.js.modules.SystemModule
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceChangeListener
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManifestBuilder
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.utils.getClientResourceHash
import java.util.WeakHashMap

class PeriscopeGatewayContext(private val context: GatewayContext) :
    EmbrGatewayContext by EmbrGatewayContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeGatewayContext
    }

    val servletManager = ModuleServletManager(context.webResourceManager, "/embr/periscope")
    val clientResourceManager = ClientResourceManager()
    val clientResourceManifestBuilder = ClientResourceManifestBuilder(clientResourceManager)

    val perspectiveContext: GatewayHook.PerspectiveGatewayContext
    val sessionMonitor: PerspectiveSessionMonitor
    private val components =
        listOf(
            EmbeddedView.asGatewayComponent { EmbeddedViewModelDelegate(it) },
            FlexRepeater.asGatewayComponent { FlexRepeaterModelDelegate(it) },
            JsonView.asGatewayComponent { JsonViewModelDelegate(it) },
            PortalLegacy.asGatewayComponent(),
            Swiper.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
            ReactLegacy.asGatewayComponent { JavaScriptProxyableComponentModelDelegate(it) },
        )

    private val clientResourceDefinitions =
        listOf(
            CssModuleResource.Descriptor,
            TypeScriptResource.Descriptor,
            ComponentResource.Descriptor,
        )

    init {
        instance = this
        perspectiveContext =
            PerspectiveContext.get(context) as GatewayHook.PerspectiveGatewayContext
        sessionMonitor = PerspectiveSessionMonitor(perspectiveContext)
    }

    val componentDefaultsManager = ProjectComponentDefaultsManager(perspectiveContext)
    val assetHandler = AssetHandler(javaClass, PeriscopeComponents.REQUIRED_RESOURCES)

    private val runtimeHash by lazy {
        val version = moduleManager.getModule(Meta.MODULE_ID)?.info?.version?.toString()
        val modules = (SystemModule.all + GlobalModule.all).map { it.emit() }
        (listOfNotNull(version) + assetHandler.hashes + modules)
            .map { it.toByteArray() }
            .sha256Delimited()
            .toHex()
    }

    private val delegateScriptManager =
        ProjectComponentDelegateScriptManager(context.projectManager)
    private val projectComponentRegistry =
        ProjectComponentRegistry(perspectiveContext, context.projectManager) {
            ProjectComponentModelDelegate(it, componentDefaultsManager, delegateScriptManager)
        }
    private val projectLifecycles =
        listOf(
            ClientResourceChangeListener(sessionMonitor, context.projectManager),
            delegateScriptManager,
            projectComponentRegistry,
        )
    private val viewLoaders = WeakHashMap<PageModel, ViewLoader>()

    fun getClientResourceHash(project: Project): String {
        return project.getClientResourceHash(runtimeHash)
    }

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

    fun applyAssetVersions() {
        assetHandler.applyVersionHashes()
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
        componentDefaultsManager.startup()
    }

    fun shutdownComponentDefaults() {
        componentDefaultsManager.shutdown()
    }

    fun removeProjectComponents() {
        projectComponentRegistry.clear()
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
}
