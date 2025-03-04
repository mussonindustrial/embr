package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.jsonedit.DocumentNode
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonPrimitive
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.designer.api.SuggestionSource
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import java.util.concurrent.CompletableFuture

class ComponentIdSuggestionSource(private val context: PeriscopeDesignerContext) :
    SuggestionSource {

    companion object {
        const val ID = "embr-periscope-component-id"
    }

    override fun getSuggestions(
        node: DocumentNode?,
        schema: JsonSchema?
    ): CompletableFuture<MutableMap<String, JsonElement>> {

        val components = context.perspectiveDesignerInterface.designerComponentRegistry.get()
        val componentIds =
            components
                .map { it.key to JsonPrimitive(it.key) as JsonElement }
                .toMap()
                .toSortedMap()
                .toMutableMap()

        return CompletableFuture.completedFuture(componentIds)
    }
}
