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

class WebJarServlet : HttpServlet() {

    private val logger = this.getLogger()
    private val context = WebAssetsGatewayContext.instance

    private val classLoader = context.webjarClassLoader

    companion object {
        fun register(servletManager: ModuleServletManager) {
            servletManager.addServlet("/webjars/*", WebJarServlet::class.java)
        }

        fun unregister(servletManager: ModuleServletManager) {
            servletManager.removeServlet("/webjars/*")
        }
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Get request received: {}", request)

        val requestedPathString =
            request.requestURI.substring(
                request.contextPath.length + request.servletPath.length + 1
            )

        val assetPath = "META-INF/resources/webjars/$requestedPathString"
        val requestedPath = File(assetPath)

        val asset = classLoader.getResourceAsStream(assetPath)
        if (asset == null) {
            logger.warn("Received request for a resource that doesn't exist: {}", assetPath)
            response.sendError("file not found")
            return
        }

        response.contentType = getMimeTypeFromExtension(requestedPath.extension)
        asset.use { inputStream ->
            response.outputStream.use { outputStream -> inputStream.transferTo(outputStream) }
        }
    }
}
