package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.inductiveautomation.perspective.gateway.api.SessionScope
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.utils.getClientResourceHash
import com.mussonindustrial.ignition.embr.periscope.utils.getHashKey
import jakarta.servlet.http.HttpServletResponse
import java.util.EnumSet
import kotlin.jvm.optionals.getOrNull

class ClientResourceManifestHandler(val context: PeriscopeGatewayContext) : RouteHandler {

    val logger = this.getLoggerEx()

    fun mount(routeMounter: RouteGroup.RouteMounter) {
        routeMounter
            .method(HttpMethod.GET)
            .type("application/json")
            .accessControl(context.requireSession(EnumSet.allOf(SessionScope::class.java)))
            .handler(this)
            .mount()
    }

    override fun handle(request: RequestContext, response: HttpServletResponse) {
        val projectName = request.getParameter("project_name")
        logger.trace("Manifest request for project: $projectName")

        if (projectName == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No project name provided.")
            return
        }

        val librariesArray = JsonArray()
        val collection =
            context.projectManager
                .find(
                    projectName,
                    ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type)),
                )
                .getOrNull()
        if (collection == null) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "No resource collection found for project: $projectName.",
            )
            return
        }

        val resources = collection.getResourcesOfType(ClientResource.type)
        logger.trace("Found ${resources.size} Client resources")

        val hash = collection.getClientResourceHash()

        val resourcesArray =
            JsonArray().apply {
                resources
                    .sortedBy { it.resourcePath.path.toString() }
                    .forEach {
                        val clientResource =
                            context.clientResourceManager.fromResource(it) ?: return@forEach

                        add(
                            JsonObject().apply {
                                addProperty("path", it.resourcePath.path.toString())
                                addProperty("type", clientResource.type.key)
                                addProperty("hash", it.getHashKey())
                            }
                        )
                    }
            }

        val json =
            JsonObject().apply {
                addProperty("hash", hash)
                add("resources", resourcesArray)
                add("libraries", librariesArray)
            }

        response.setHeader("Cache-Control", "no-store")
        response.sendSuccess(json)
        return
    }
}
