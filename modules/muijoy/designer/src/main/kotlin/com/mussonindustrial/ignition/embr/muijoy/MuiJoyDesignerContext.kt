package com.mussonindustrial.ignition.embr.muijoy

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.embr.perspective.designer.component.asDesignerComponent
import com.mussonindustrial.embr.perspective.designer.component.registerComponent
import com.mussonindustrial.embr.perspective.designer.component.removeComponent
import com.mussonindustrial.ignition.embr.muijoy.component.ComponentIdSuggestionSource
import com.mussonindustrial.ignition.embr.muijoy.component.input.*

class MuiJoyDesignerContext(private val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: MuiJoyDesignerContext
    }

    val perspectiveDesignerInterface: PerspectiveDesignerInterface
    private val componentIdSuggestionSource: ComponentIdSuggestionSource
    private val components = MuiJoyComponents.components.map { it.asDesignerComponent() }

    init {
        instance = this
        perspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)
        componentIdSuggestionSource = ComponentIdSuggestionSource(this)
        perspectiveDesignerInterface.suggestionSourceRegistry.registerSuggestionSource(
            ComponentIdSuggestionSource.ID,
            componentIdSuggestionSource,
        )
    }

    fun registerComponents() {
        components.forEach { perspectiveDesignerInterface.registerComponent(it) }
    }

    fun removeComponents() {
        components.forEach { perspectiveDesignerInterface.removeComponent(it) }
    }
}
