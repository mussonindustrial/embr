package com.mussonindustrial.ignition.embr.webassets.servlets

import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.gateway.api.getMimeTypeFromExtension
import com.mussonindustrial.embr.gateway.api.sendError
import com.mussonindustrial.embr.servlets.ModuleServletManager
import com.mussonindustrial.ignition.embr.webassets.WebAssetsGatewayContext
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WebResourcesServlet : HttpServlet() {

    private val logger = this.getLogger()
    private val context = WebAssetsGatewayContext.instance

    private val rootPath = context.resourcesFolder

    companion object {
        fun register(servletManager: ModuleServletManager) {
            servletManager.addServlet("/resources/*", WebResourcesServlet::class.java)
        }

        fun unregister(servletManager: ModuleServletManager) {
            servletManager.removeServlet("/resources/*")
        }
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
