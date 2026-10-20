package com.mussonindustrial.embr.servlets

import com.inductiveautomation.ignition.gateway.dataroutes.HttpMethod
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.dataroutes.RouteHandler
import com.mussonindustrial.embr.common.logging.getLoggerEx
import java.net.URLConnection
import java.net.URLDecoder
import javax.servlet.http.HttpServletResponse

class ClassLoaderResourceHandler(
    private val classLoader: ClassLoader,
    private val resourceRootPath: String = "",
    private val mountedRoute: String = "",
) : RouteHandler {

    private val logger = this.getLoggerEx()

    companion object {
        private const val CACHE_FOREVER = "public, max-age=31536000, immutable"
        private const val NO_CACHE = "no-cache, no-store, must-revalidate"
        private val HASHED_FILE_REGEX =
            Regex(".*\\.([a-f0-9]{8,32})\\.[a-z0-9]+$", RegexOption.IGNORE_CASE)
    }

    fun mount(mounter: RouteGroup.RouteMounter) {
        mounter.method(HttpMethod.GET).handler(this).mount()
    }

    override fun handle(context: RequestContext, response: HttpServletResponse): Any? {
        val request = context.request
        logger.trace("Module Resource request: ${request.requestURI}")

        val safePath =
            URLDecoder.decode(request.pathInfo ?: "/", Charsets.UTF_8)
                .substringBefore(';')
                .substringBefore('?')
                .substringAfter(mountedRoute)
                .removePrefix("/")

        if (safePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory listing denied")
            return null
        }

        if (safePath.contains("..") || safePath.contains("\u0000")) {
            logger.warn("Attempted path traversal or invalid path: $safePath")
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid resource path")
            return null
        }

        val hash = HASHED_FILE_REGEX.matchEntire(safePath)?.groupValues?.get(1)
        val isHashed = hash != null
        val unhashedPath = if (isHashed) safePath.replace(".$hash.", ".") else safePath

        val resourcePath = "$resourceRootPath/$unhashedPath"

        logger.trace("Resolved classpath resource: $resourcePath")

        if (isHashed) {
            val eTag = "\"$hash\""
            if (request.getHeader("If-None-Match") == eTag) {
                response.status = HttpServletResponse.SC_NOT_MODIFIED
                return null
            }
            response.setHeader("ETag", eTag)
        }

        classLoader.getResourceAsStream(resourcePath)?.use { stream ->
            logger.trace("Sending resource: $resourcePath")

            response.apply {
                contentType =
                    request.servletContext.getMimeType(resourcePath)
                        ?: URLConnection.guessContentTypeFromName(resourcePath)
                        ?: "application/octet-stream"

                characterEncoding = Charsets.UTF_8.name()
                setHeader("X-Content-Type-Options", "nosniff")

                if (isHashed) {
                    setHeader("Cache-Control", CACHE_FOREVER)
                } else {
                    setHeader("Cache-Control", NO_CACHE)
                    setHeader("Pragma", "no-cache")
                    setDateHeader("Expires", 0)
                }

                stream.transferTo(outputStream)
            }
        }
            ?: run {
                logger.warn("Resource not found: $resourcePath")
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found")
            }

        return null
    }
}
