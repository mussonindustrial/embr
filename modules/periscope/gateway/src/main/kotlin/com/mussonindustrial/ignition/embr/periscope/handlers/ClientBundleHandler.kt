package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.mussonindustrial.embr.common.cache.LruCache
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.cacheForever
import com.mussonindustrial.embr.perspective.gateway.routes.SessionRouteHandler
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.js.ClientResourceImportRewriter
import com.mussonindustrial.ignition.embr.periscope.js.RewrittenModule
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import com.mussonindustrial.ignition.embr.periscope.utils.findClientResources
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import javax.servlet.http.HttpServletResponse

class ClientBundleHandler(private val context: PeriscopeGatewayContext) :
    SessionRouteHandler("application/json") {

    companion object {
        private const val MAX_CACHED_BUNDLES = 8
    }

    private val logger = this.getLoggerEx()

    private val cachedBundles = LruCache<String, ByteArray>(MAX_CACHED_BUNDLES)

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Client bundle request: $request")

        val projectName =
            request.getParameter("project_name")
                ?: return response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing project name.",
                )

        val hash =
            request.getParameter("hash")
                ?: return response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing hash.")

        val bundle =
            cachedBundles[keyOf(projectName, hash)]
                ?: buildBundle(projectName, hash)
                ?: return response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Bundle '${keyOf(projectName, hash)}' not found.",
                )

        response.apply {
            contentType = "application/json"
            setHeader("Content-Encoding", "gzip")
            setContentLength(bundle.size)
            cacheForever()
            outputStream.use { it.write(bundle) }
        }
    }

    private fun buildBundle(projectName: String, hash: String): ByteArray? {
        val project = context.projectManager.findClientResources(projectName) ?: return null
        if (context.getClientResourceHash(project) != hash) {
            return null
        }

        val manifest = context.clientResourceManifestBuilder.createManifest(project, hash)
        val json =
            JsonObject().apply {
                addProperty("hash", hash)
                add("manifest", manifest)
                add("modules", createModules(projectName, project, hash))
            }

        val bundle = gzip(json.toString())
        cachedBundles[keyOf(projectName, hash)] = bundle
        return bundle
    }

    private fun createModules(
        projectName: String,
        project: RuntimeProject,
        hash: String,
    ): JsonObject {
        return JsonObject().apply {
            project.getResourcesOfType(ClientResource.type).forEach { resource ->
                val clientResource =
                    context.clientResourceManager.fromResource(resource) ?: return@forEach
                val compiled = clientResource.fileContents.compiled
                if (clientResource.contentType.contains("javascript") && compiled.isNotEmpty()) {
                    val path = resource.resourcePath.pathString
                    val rewriter = ClientResourceImportRewriter(projectName, hash, path)
                    add(path, createModule(rewriter.rewriteModule(compiled)))
                }
            }
        }
    }

    private fun createModule(module: RewrittenModule): JsonObject {
        val imports =
            JsonArray().apply {
                module.imports.forEach { range ->
                    add(
                        JsonArray().apply {
                            add(range.first)
                            add(range.last + 1)
                        },
                    )
                }
            }

        return JsonObject().apply {
            addProperty("source", module.source)
            add("imports", imports)
        }
    }

    private fun keyOf(projectName: String, hash: String): String {
        return "$projectName/$hash"
    }

    private fun gzip(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(text.toByteArray()) }
        return output.toByteArray()
    }
}
