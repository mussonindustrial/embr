package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.utils.findClientResources
import jakarta.servlet.http.HttpServletResponse
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ClientBundleHandler(context: PeriscopeGatewayContext) :
    SessionRouteHandler(context, "application/json") {

    val logger = this.getLoggerEx()

    private val cachedBundles =
        object : LinkedHashMap<String, ByteArray>(MAX_CACHED_BUNDLES, 0.75f, true) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<String, ByteArray>,
            ): Boolean {
                return size > MAX_CACHED_BUNDLES
            }
        }

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client Bundle request: $request")

        val projectName = request.getParameter("project_name").orEmpty()
        val hash = request.getParameter("hash").orEmpty()

        val bundle = getCachedBundle(projectName, hash) ?: buildBundle(projectName, hash)
        if (bundle == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Bundle not found: $projectName/$hash",
            )
            return
        }

        response.apply {
            contentType = "application/json"
            setHeader("Content-Encoding", "gzip")
            setContentLength(bundle.size)
            cacheForever()
            outputStream.use { it.write(bundle) }
        }
    }

    private fun getCachedBundle(projectName: String, hash: String): ByteArray? {
        synchronized(cachedBundles) {
            return cachedBundles["$projectName/$hash"]
        }
    }

    private fun buildBundle(projectName: String, hash: String): ByteArray? {
        val collection = context.projectManager.findClientResources(projectName) ?: return null
        if (context.clientResourceHash(collection) != hash) {
            return null
        }

        val json =
            JsonObject().apply {
                addProperty("hash", hash)
                add("manifest", context.clientResourceManifests.createManifest(collection, hash))
                add("modules", createModules(projectName, collection, hash))
            }

        val bundle = gzip(json.toString())
        synchronized(cachedBundles) { cachedBundles["$projectName/$hash"] = bundle }
        return bundle
    }

    private fun createModules(
        projectName: String,
        collection: RuntimeResourceCollection,
        hash: String,
    ): JsonObject {
        val rewriter = ClientResourceImportRewriter(projectName, hash)
        val modules = JsonObject()

        for (resource in collection.getResourcesOfType(ClientResource.type)) {
            val clientResource = context.clientResourceManager.fromResource(resource) ?: continue
            val compiled = clientResource.fileContents.compiled
            if (clientResource.contentType.contains("javascript") && compiled.isNotEmpty()) {
                modules.addProperty(
                    resource.resourcePath.path.toString(),
                    rewriter.rewrite(compiled),
                )
            }
        }

        return modules
    }

    private fun gzip(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(text.toByteArray()) }
        return output.toByteArray()
    }

    companion object {
        private const val MAX_CACHED_BUNDLES = 8
    }
}
