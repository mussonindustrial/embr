package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.jsonedit.DocumentNode
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonPrimitive
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.designer.api.SuggestionSource
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import java.util.concurrent.CompletableFuture

class TypeScriptResourceExportSuggestionSource(private val context: PeriscopeDesignerContext) :
    SuggestionSource {

    companion object {
        const val ID = "embr-periscope-typescript-resource-export"
        private const val RESOURCE_PATH = "path"
    }

    override fun getSuggestions(
        node: DocumentNode,
        schema: JsonSchema,
    ): CompletableFuture<MutableMap<String, JsonElement>> {
        val resourcePath = node.parent?.findChild(RESOURCE_PATH)?.orElse(null)?.value as? String
        if (resourcePath.isNullOrEmpty()) {
            return CompletableFuture.completedFuture(mutableMapOf())
        }

        return context.typeScriptWorkspace.service.reactComponentNames(resourcePath).thenApply {
            exports ->
            exports
                ?.names
                .orEmpty()
                .associateWith { JsonPrimitive(it) as JsonElement }
                .toSortedMap()
                .toMutableMap()
        }
    }
}
