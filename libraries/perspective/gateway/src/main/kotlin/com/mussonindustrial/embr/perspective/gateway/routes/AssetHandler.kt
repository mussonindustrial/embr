package com.mussonindustrial.embr.perspective.gateway.routes

import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.embr.common.crypto.sha256
import com.mussonindustrial.embr.common.crypto.toHex
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.gateway.api.cacheForever
import com.mussonindustrial.embr.gateway.api.cacheRevalidate
import jakarta.servlet.http.HttpServletResponse

class AssetHandler(source: Class<*>, private val resources: Collection<BrowserResource>) :
    SessionRouteHandler() {

    companion object {
        private const val STATIC_FOLDER = "/static"
        private const val HASH_LENGTH = 12
        private val VERSIONED_NAME = Regex("""^(.+?)\.([0-9a-f]{$HASH_LENGTH})(\.[a-z]+)$""")
    }

    private val logger = this.getLoggerEx()
    private val BrowserResource.fileName: String
        get() = path.substringAfterLast('/')

    private val assetsByName = loadAssets(source)

    val hashes: List<String>
        get() = assetsByName.values.map { it.hash }.sorted()

    fun applyVersionHashes() {
        resources.forEach { resource ->
            assetsByName[resource.fileName]?.let { resource.versionHash = it.hash }
        }
    }

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        val fileName = request.path.substringAfterLast('/')
        val versionedName = VERSIONED_NAME.matchEntire(fileName)
        val assetName = versionedName?.let { it.groupValues[1] + it.groupValues[3] } ?: fileName

        val asset = assetsByName[assetName]
        if (asset == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        response.contentType = asset.contentType
        if (versionedName?.groupValues?.get(2) == asset.hash) {
            response.cacheForever()
        } else {
            response.cacheRevalidate()
        }

        response.outputStream.use { it.write(asset.bytes) }
    }

    private fun loadAssets(source: Class<*>): Map<String, Asset> {
        val assets = mutableMapOf<String, Asset>()
        resources.forEach { resource ->
            val stream = source.getResourceAsStream("$STATIC_FOLDER/${resource.fileName}")
            if (stream == null) {
                logger.warn("Failed to load asset '${resource.fileName}'.")
            } else {
                stream.use {
                    assets[resource.fileName] = Asset(it.readBytes(), getContentType(resource))
                }
            }
        }
        return assets
    }

    private fun getContentType(resource: BrowserResource): String {
        return when (resource.type) {
            BrowserResource.ResourceType.CSS -> "text/css"
            else -> "text/javascript"
        }
    }

    private class Asset(val bytes: ByteArray, val contentType: String) {
        val hash = bytes.sha256().toHex().take(HASH_LENGTH)
    }
}
