package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.mussonindustrial.embr.common.cache.LruCache
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter.Companion.PROJECT_PREFIX
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter.Companion.SYSTEM_PREFIX
import com.mussonindustrial.ignition.embr.periscope.js.JavaScriptImportScanner
import com.mussonindustrial.ignition.embr.periscope.utils.getHashKey

class ClientResourceManifestBuilder(private val clientResourceManager: ClientResourceManager) {

    companion object {
        private val PACKAGE_NAME = Regex("""^[A-Za-z@][\w@/.-]*$""")
        private const val MAX_CACHED_IMPORTS = 10_000
    }

    private val cachedImports = LruCache<String, List<String>>(MAX_CACHED_IMPORTS)

    fun createManifest(project: RuntimeProject, hash: String): JsonObject {
        val resources =
            project.getResourcesOfType(ClientResource.type).sortedBy {
                it.resourcePath.pathString
            }

        val resourcesArray =
            JsonArray().apply { resources.mapNotNull { createEntry(it) }.forEach { add(it) } }

        return JsonObject().apply {
            addProperty("hash", hash)
            add("resources", resourcesArray)
        }
    }

    private fun createEntry(resource: ProjectResource): JsonObject? {
        val clientResource = clientResourceManager.fromResource(resource) ?: return null
        val path = resource.resourcePath.pathString
        val hashKey = resource.getHashKey()

        val importsArray =
            JsonArray().apply {
                findImports(path, hashKey, clientResource.fileContents.compiled).forEach { add(it) }
            }

        return JsonObject().apply {
            addProperty("path", path)
            addProperty("type", clientResource.type.key)
            addProperty("hash", hashKey)
            add("imports", importsArray)
            if (clientResource is ComponentResource) {
                add("component", createComponent(clientResource.attributes))
            }
        }
    }

    private fun createComponent(attributes: ComponentAttributes): JsonObject {
        return JsonObject().apply {
            addProperty("id", attributes.id.takeIf { attributes.hasValidId }.orEmpty())
            addProperty("name", attributes.name)
        }
    }

    private fun findImports(path: String, hashKey: String, source: String): List<String> {
        return cachedImports.getOrPut("$path:$hashKey") {
            JavaScriptImportScanner(source)
                .scan()
                .mapNotNull { resolveImport(path, it.specifier) }
                .distinct()
        }
    }

    private fun resolveImport(path: String, specifier: String): String? {
        if (specifier.startsWith(PROJECT_PREFIX)) {
            return specifier.removePrefix(PROJECT_PREFIX)
        }

        if (ClientResourceImportRewriter.isRelative(specifier)) {
            return ClientResourceImportRewriter.resolveRelative(path, specifier)
        }

        if (specifier.startsWith(SYSTEM_PREFIX) || PACKAGE_NAME.matches(specifier)) {
            return specifier
        }

        return null
    }
}
