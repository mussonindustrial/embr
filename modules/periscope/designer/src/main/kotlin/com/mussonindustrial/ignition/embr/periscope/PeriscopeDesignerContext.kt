package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.embr.perspective.designer.component.asDesignerComponent
import com.mussonindustrial.embr.perspective.designer.component.registerComponent
import com.mussonindustrial.embr.perspective.designer.component.removeComponent
import com.mussonindustrial.ignition.embr.periscope.component.ComponentIdSuggestionSource
import com.mussonindustrial.ignition.embr.periscope.component.embedding.*

class PeriscopeDesignerContext(private val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeDesignerContext
    }

    val perspectiveDesignerInterface: PerspectiveDesignerInterface
    private val componentIdSuggestionSource: ComponentIdSuggestionSource
    private val components =
        listOf(
            EmbeddedView.asDesignerComponent(),
            FlexRepeater.asDesignerComponent(),
            JsonView.asDesignerComponent(),
            Portal.asDesignerComponent(),
            Swiper.asDesignerComponent(),
        )

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
