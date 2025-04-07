package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import com.mussonindustrial.embr.designer.EmbrDesignerContextImpl
import com.mussonindustrial.ignition.embr.periscope.component.ComponentIdSuggestionSource

class PeriscopeDesignerContext(val context: DesignerContext) :
    EmbrDesignerContext by EmbrDesignerContextImpl(context) {
    companion object {
        lateinit var instance: PeriscopeDesignerContext
    }

    val perspectiveDesignerInterface: PerspectiveDesignerInterface
    private val componentIdSuggestionSource: ComponentIdSuggestionSource

    init {
        instance = this
        perspectiveDesignerInterface = PerspectiveDesignerInterface.get(context)
        componentIdSuggestionSource = ComponentIdSuggestionSource(this)
        perspectiveDesignerInterface.suggestionSourceRegistry.registerSuggestionSource(
            ComponentIdSuggestionSource.ID,
            componentIdSuggestionSource,
        )
    }
}
