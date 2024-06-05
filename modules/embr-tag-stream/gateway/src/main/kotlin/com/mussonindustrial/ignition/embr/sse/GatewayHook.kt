package com.mussonindustrial.ignition.embr.sse

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.sse.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.sse.Meta.URL_ALIAS
import com.mussonindustrial.ignition.embr.sse.servlets.TagStreamCreationServlet
import com.mussonindustrial.ignition.embr.sse.servlets.TagStreamServlet
import com.mussonindustrial.ignition.embr.sse.servlets.addUnmappedServlet
import com.mussonindustrial.ignition.embr.sse.servlets.removeUnmappedServlet
import com.mussonindustrial.ignition.embr.sse.tags.TagStreamManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {

    companion object {
        lateinit var context: GatewayContext
        lateinit var tagStreamManager: TagStreamManager
    }

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: GatewayContext

    override fun setup(context: GatewayContext) {
        this.context = context
        GatewayHook.context = context
        tagStreamManager =  TagStreamManager(context.tagManager)
    }


    override fun startup(activationState: LicenseState) {
        logger.info("Embr Tag Stream module started.")
        context.webResourceManager.addUnmappedServlet("/", TagStreamServlet::class.java)
        context.webResourceManager.addServlet("subscribe", TagStreamCreationServlet::class.java)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Tag Stream module.")
        context.webResourceManager.removeUnmappedServlet("/")
        context.webResourceManager.removeServlet("subscribe")
    }

    override fun getMountedResourceFolder(): Optional<String>? {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String>? {
        return Optional.of(URL_ALIAS)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}