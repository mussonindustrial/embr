package com.mussonindustrial.ignition.embr.periscope.servlets

import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.gateway.api.getMimeTypeFromExtension
import com.mussonindustrial.embr.gateway.api.sendError
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WebLibraryServlet : HttpServlet() {

    private val logger = this.getLogger()
    private val context = PeriscopeGatewayContext.instance

    private val rootPath =
        context.systemManager.dataDir.resolve("modules/${Meta.MODULE_ID}/web-library")

    init {
        rootPath.mkdirs()
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Get request received: {}", request)

        val requestedPathString =
            request.requestURI.substring(
                request.contextPath.length + request.servletPath.length + 1
            )
        val requestedPath = File(requestedPathString)

        if (requestedPath.isAbsolute) {
            logger.warn("Received request for absolute resource path: {}", requestedPath)
            response.sendError("invalid path")
            return
        }

        val resolvedPath = rootPath.resolve(requestedPath).normalize()
        if (!resolvedPath.startsWith(rootPath)) {
            logger.warn("Received request for a path that escapes resource root: {}", resolvedPath)
            response.sendError("invalid path")
            return
        }

        if (!resolvedPath.exists()) {
            logger.warn("Received request for a resource that doesn't exist: {}", resolvedPath)
            response.sendError("file not found")
            return
        }

        response.contentType = getMimeTypeFromExtension(resolvedPath.extension)
        resolvedPath.inputStream().use { inputStream ->
            response.outputStream.use { outputStream -> inputStream.transferTo(outputStream) }
        }
    }
}
