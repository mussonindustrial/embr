package com.mussonindustrial.embr.eventstream

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.eventstream.servlets.EventStreamSessionServlet
import com.mussonindustrial.embr.eventstream.servlets.EventStreamServlet
import com.mussonindustrial.embr.eventstream.servlets.TagHistoryServlet
import com.mussonindustrial.embr.eventstream.streams.LicenseStream
import com.mussonindustrial.embr.eventstream.streams.TagStream
import java.util.Optional

@Suppress("unused")
class EventStreamGatewayHook : AbstractGatewayModuleHook() {
    private val logger = this.getLogger()
    private lateinit var context: EventStreamGatewayContext

    override fun setup(context: GatewayContext) {
        this.context = EventStreamGatewayContext(context)
        this.context.eventStreamManager.registerStreamType(LicenseStream.key, LicenseStream::get)
        this.context.eventStreamManager.registerStreamType(TagStream.key, TagStream::get)
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr Event Stream module started.")
        context.servletManager.addServlet("/session", EventStreamSessionServlet::class.java)
        context.servletManager.addServlet("/session/*", EventStreamServlet::class.java)
        context.servletManager.addServlet("/history", TagHistoryServlet::class.java)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Event Stream module.")
        context.eventStreamManager.closeAllSessions()
        context.servletManager.removeAllServlets()
        context.eventStreamExecutionManager.shutdown()
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
