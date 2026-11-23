package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerHook
import com.inductiveautomation.perspective.designer.PerspectiveNavNode
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.embr.perspective.designer.component.asDesignerComponent
import com.mussonindustrial.embr.perspective.designer.component.registerComponent
import com.mussonindustrial.embr.perspective.designer.component.removeComponent
import com.mussonindustrial.ignition.embr.periscope.action.JavaScriptActionDesignDelegate
import com.mussonindustrial.ignition.embr.periscope.component.ComponentIdSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.ComponentPaletteFrame
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponentRegistry
import com.mussonindustrial.ignition.embr.periscope.component.TypeScriptResourceExportSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.TypeScriptResourceSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.event.PeriscopeComponentEvents
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.js.PeriscopeDesignerBridge
import com.mussonindustrial.ignition.embr.periscope.navtree.model.ComponentResourceNode
import com.mussonindustrial.ignition.embr.periscope.navtree.model.CssModuleResourceNode
import com.mussonindustrial.ignition.embr.periscope.navtree.model.TypeScriptResourceNode
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.asDesignerDescriptor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.ComponentResourceEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.CssModuleResourceEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.TypeScriptResourceEditor
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptLanguageService
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptWorkspace
import com.teamdev.jxbrowser.engine.Engine
import java.awt.AWTEvent
import java.awt.Toolkit

class PeriscopeDesignerContext(val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeDesignerContext
    }

    val perspectiveDesignerInterface: PerspectiveDesignerInterface
    val perspectiveNavNode: PerspectiveNavNode
    val jxBrowserEngine: Engine
    val bridge: PeriscopeDesignerBridge
    val clientResourceManager = ClientResourceManager()

    init {
        instance = this
        perspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)
        perspectiveNavNode =
            DesignerHook.get(context).getPrivateProperty("navNode") as PerspectiveNavNode
        jxBrowserEngine = DesignerHook.get(context).workspace.engine
        bridge = PeriscopeDesignerBridge(jxBrowserEngine)
        perspectiveDesignerInterface.suggestionSourceRegistry.apply {
            registerSuggestionSource(
                ComponentIdSuggestionSource.ID,
                ComponentIdSuggestionSource(this@PeriscopeDesignerContext),
            )
            registerSuggestionSource(
                TypeScriptResourceSuggestionSource.ID,
                TypeScriptResourceSuggestionSource(this@PeriscopeDesignerContext),
            )
            registerSuggestionSource(
                TypeScriptResourceExportSuggestionSource.ID,
                TypeScriptResourceExportSuggestionSource(this@PeriscopeDesignerContext),
            )
        }
        perspectiveDesignerInterface.actionRegistry.apply {
            registerAction(JavaScriptActionDesignDelegate())
        }
    }

    val typeScriptWorkspace =
        TypeScriptWorkspace(
            context,
            clientResourceManager,
            TypeScriptLanguageService(bridge),
        )

    private val projectComponentRegistry =
        ProjectComponentRegistry(context, perspectiveDesignerInterface, typeScriptWorkspace)

    val componentPalette =
        ComponentPaletteFrame(context, projectComponentRegistry, ::openClientResource)

    private val components =
        listOf(
            EmbeddedView.asDesignerComponent(),
            FlexRepeater.asDesignerComponent(),
            JsonView.asDesignerComponent(),
            Portal.asDesignerComponent(),
            Swiper.asDesignerComponent(),
            React.asDesignerComponent(),
        )

    private val clientResourceWorkspace =
        ClientResourceWorkspace(context, perspectiveNavNode, clientResourceManager)

    private val workspaces = listOf(clientResourceWorkspace)

    private val clientResourceDefinitions =
        listOf(
            CssModuleResource.asDesignerDescriptor(
                PeriscopeIcons.css,
                CssModuleResourceNode.factory,
                CssModuleResourceEditor.factory,
            ),
            TypeScriptResource.asDesignerDescriptor(
                PeriscopeIcons.tsx,
                TypeScriptResourceNode.factory,
                TypeScriptResourceEditor.factory,
            ),
            ComponentResource.asDesignerDescriptor(
                PeriscopeIcons.component,
                ComponentResourceNode.factory,
                ComponentResourceEditor.factory,
            ),
        )

    fun registerComponents() {
        components.forEach { perspectiveDesignerInterface.registerComponent(it) }
    }

    fun removeComponents() {
        components.forEach { perspectiveDesignerInterface.removeComponent(it) }
    }

    fun registerResourceWorkspaces() {
        workspaces.forEach { registerResourceWorkspace(it) }
    }

    fun commitResourceEditors() {
        workspaces.forEach { it.commitAll() }
    }

    fun registerClientResourceDefinitions() {
        clientResourceDefinitions.forEach { clientResourceManager.register(it) }
    }

    fun startupTypeScriptWorkspace() {
        typeScriptWorkspace.startup()
    }

    fun shutdownTypeScriptWorkspace() {
        typeScriptWorkspace.shutdown()
        bridge.close()
    }

    fun startupProjectComponents() {
        projectComponentRegistry.startup()
    }

    fun shutdownProjectComponents() {
        projectComponentRegistry.shutdown()
    }

    fun startupComponentPalette() {
        componentPalette.startup()
    }

    fun shutdownComponentPalette() {
        componentPalette.shutdown()
    }

    fun registerAWTEventListeners() {
        Toolkit.getDefaultToolkit()
            .addAWTEventListener(PeriscopeComponentEvents, AWTEvent.WINDOW_EVENT_MASK)
    }

    fun removeAWTEventListeners() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(PeriscopeComponentEvents)
    }

    private fun openClientResource(path: String) {
        clientResourceWorkspace.open(ResourcePath(ClientResource.type, path))
        (frame as IgnitionDesigner).workspace.setSelectedWorkspace(clientResourceWorkspace.key)
    }
}
