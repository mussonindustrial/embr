package com.mussonindustrial.ignition.embr.periscope.handlers

import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import java.security.MessageDigest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.codec.binary.Hex

class AssetHandler(context: PeriscopeGatewayContext) : SessionRouteHandler(context) {

    private class Asset(val bytes: ByteArray, val contentType: String) {
        val hash: String =
            Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(bytes)).take(12)
    }

    val logger = this.getLoggerEx()

    private val assetsByName = loadAssets()

    val hashes: List<String>
        get() = assetsByName.values.map { it.hash }.sorted()

    private fun loadAssets(): Map<String, Asset> {
        val assets = mutableMapOf<String, Asset>()
        for ((name, contentType) in STATIC_ASSETS) {
            val stream = AssetHandler::class.java.getResourceAsStream("/static/$name") ?: continue
            assets[name] = stream.use { Asset(it.readBytes(), contentType) }
        }
        return assets
    }

    fun version(resources: Collection<BrowserResource>) {
        for (resource in resources) {
            val asset = assetsByName[resource.path.substringAfterLast('/')] ?: continue
            resource.versionHash = asset.hash
        }
    }

    override fun respond(request: RequestContext, response: HttpServletResponse) {
        logger.trace("Asset request: $request")

        val fileName = request.path.substringAfterLast('/')
        val versionedName = VERSIONED_NAME.matchEntire(fileName)

        val assetName =
            if (versionedName != null) {
                versionedName.groupValues[1] + versionedName.groupValues[3]
            } else {
                fileName
            }

        val asset = assetsByName[assetName]
        if (asset == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        response.contentType = asset.contentType
        if (versionedName != null && versionedName.groupValues[2] == asset.hash) {
            response.cacheForever()
        } else {
            response.revalidate()
        }

        response.outputStream.use { it.write(asset.bytes) }
    }

    companion object {
        private val STATIC_ASSETS =
            mapOf(
                "embr-periscope-client.js" to "text/javascript",
                "embr-periscope.css" to "text/css",
            )
        private val VERSIONED_NAME = Regex("""^(.+?)\.([0-9a-f]{12})(\.[a-z]+)$""")
    }
}
