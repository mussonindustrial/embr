package com.mussonindustrial.ignition.embr.periscope

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
import com.mussonindustrial.ignition.embr.periscope.component.ComponentIdSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.TypeScriptResourceSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.js.PeriscopeDesignerBridge
import com.mussonindustrial.ignition.embr.periscope.navtree.model.CssModuleResourceNode
import com.mussonindustrial.ignition.embr.periscope.navtree.model.TypeScriptResourceNode
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.asDesignerDescriptor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.CssModuleResourceEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.TypeScriptResourceEditor
import com.teamdev.jxbrowser.engine.Engine

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
        }
    }

    private val components =
        listOf(
            EmbeddedView.asDesignerComponent(),
            FlexRepeater.asDesignerComponent(),
            JsonView.asDesignerComponent(),
            Portal.asDesignerComponent(),
            Swiper.asDesignerComponent(),
            React.asDesignerComponent(),
        )

    private val workspaces =
        listOf(ClientResourceWorkspace(context, perspectiveNavNode, clientResourceManager))

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

    fun registerClientResourceDefinitions() {
        clientResourceDefinitions.forEach { clientResourceManager.register(it) }
    }
}
