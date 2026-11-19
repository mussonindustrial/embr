package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.mussonindustrial.ignition.embr.periscope.js.JavaScriptImportRewriter
import com.mussonindustrial.ignition.embr.periscope.utils.getHashKey
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class ClientResourceManifests(private val manager: ClientResourceManager) {

    private val cachedImports = ConcurrentHashMap<String, List<String>>()

    fun createManifest(project: RuntimeProject, hash: String): JsonObject {
        val resources =
            project.getResourcesOfType(ClientResource.type).sortedBy {
                it.resourcePath.path.toString()
            }

        val resourcesArray = JsonArray()
        for (resource in resources) {
            val entry = createEntry(resource) ?: continue
            resourcesArray.add(entry)
        }

        return JsonObject().apply {
            addProperty("hash", hash)
            add("resources", resourcesArray)
        }
    }

    private fun createEntry(resource: ProjectResource): JsonObject? {
        val clientResource = manager.fromResource(resource) ?: return null
        val path = resource.resourcePath.path.toString()
        val hashKey = resource.getHashKey()

        val importsArray = JsonArray()
        findImports(path, hashKey, clientResource.fileContents.compiled).forEach {
            importsArray.add(it)
        }

        return JsonObject().apply {
            addProperty("path", path)
            addProperty("type", clientResource.type.key)
            addProperty("hash", hashKey)
            add("imports", importsArray)
        }
    }

    private fun findImports(path: String, hashKey: String, source: String?): List<String> {
        val cacheKey = "$path:$hashKey"
        cachedImports[cacheKey]?.let {
            return it
        }

        val imports =
            JavaScriptImportRewriter.specifiers(source.orEmpty())
                .mapNotNull { resolveImport(path, it) }
                .distinct()
                .toList()

        if (cachedImports.size >= MAX_CACHED_IMPORTS) {
            cachedImports.clear()
        }
        cachedImports[cacheKey] = imports
        return imports
    }

    private fun resolveImport(path: String, specifier: String): String? {
        if (specifier.startsWith(PROJECT_PREFIX)) {
            return specifier.removePrefix(PROJECT_PREFIX)
        }

        if (specifier.startsWith("./") || specifier.startsWith("../")) {
            return resolveRelativeImport(path, specifier)
        }

        if (specifier.startsWith(SYSTEM_PREFIX) || PACKAGE_NAME.matches(specifier)) {
            return specifier
        }

        return null
    }

    private fun resolveRelativeImport(path: String, specifier: String): String? {
        val resolvedUri =
            runCatching { URI("/$path").resolve(specifier) }.getOrNull() ?: return null
        val resolvedPath = resolvedUri.path ?: return null

        val resolved = resolvedPath.removePrefix("/")
        if (resolved.isEmpty() || resolved.startsWith("..")) {
            return null
        }
        return resolved
    }

    companion object {
        private const val PROJECT_PREFIX = "@/"
        private const val SYSTEM_PREFIX = "system:"
        private val PACKAGE_NAME = Regex("""^[A-Za-z@][\w@/.-]*$""")
        private const val MAX_CACHED_IMPORTS = 10_000
    }
}
