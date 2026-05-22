package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.jsonedit.DocumentNode
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonPrimitive
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.perspective.designer.api.SuggestionSource
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import java.util.concurrent.CompletableFuture

class TypeScriptResourceSuggestionSource(private val context: PeriscopeDesignerContext) :
    SuggestionSource {

    companion object {
        const val ID = "embr-periscope-typescript-resource"
    }

    override fun getSuggestions(
        node: DocumentNode,
        schema: JsonSchema,
    ): CompletableFuture<MutableMap<String, JsonElement>> {

        val project = context.project
        if (project == null) {
            return CompletableFuture.completedFuture(mutableMapOf())
        }

        val resources = context.project!!.getResourcesOfType(ClientResource.type)
        val resourcePaths =
            resources
                .filter { ClientResource.getType(it) == TypeScriptResource.type }
                .associate {
                    it.resourcePath.path.toString() to
                        JsonPrimitive(it.resourcePath.path.toString()) as JsonElement
                }
                .toSortedMap()
                .toMutableMap()

        return CompletableFuture.completedFuture(resourcePaths)
    }
}
