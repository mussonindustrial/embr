package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagHistoryServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamSessionServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamManagerServlet
import java.util.*


@Suppress("unused")
class TagStreamGatewayHook : AbstractGatewayModuleHook() {

    private val logger = this.getLogger()
    private lateinit var context: TagStreamGatewayContext

    override fun setup(context: GatewayContext) {
        this.context = TagStreamGatewayContext(context)
    }


    override fun startup(activationState: LicenseState) {
        logger.info("Embr Tag Stream module started.")
        context.servletManager.addServlet("/session", TagStreamManagerServlet::class.java)
        context.servletManager.addServlet("/session/*", TagStreamSessionServlet::class.java)
        context.servletManager.addServlet("/history", TagHistoryServlet::class.java)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Tag Stream module.")
        context.tagStreamManager.closeAllSessions()
        context.servletManager.removeAllServlets()
    }

    override fun getMountedResourceFolder(): Optional<String> {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String> {
        return Optional.of(Meta.shortId)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}