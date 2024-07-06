package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.tagstream.emitters.LicenseEmitter
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagHistoryServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.EventStreamServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.EventStreamSessionServlet
import com.mussonindustrial.ignition.embr.tagstream.emitters.TagEmitter
import java.util.*

@Suppress("unused")
class EventStreamGatewayHook : AbstractGatewayModuleHook() {

    private val logger = this.getLogger()
    private lateinit var context: EventStreamGatewayContext

    override fun setup(context: GatewayContext) {
        this.context = EventStreamGatewayContext(context)
        this.context.eventStreamManager.registerEmitter(LicenseEmitter.KEY, LicenseEmitter::class.java, LicenseEmitter.gsonAdapter)
        this.context.eventStreamManager.registerEmitter(TagEmitter.KEY, TagEmitter::class.java, TagEmitter.gsonAdapter)
    }

    override fun startup(activationState: LicenseState) {
        logger.info("Embr Tag Stream module started.")
        context.servletManager.addServlet("/session", EventStreamSessionServlet::class.java)
        context.servletManager.addServlet("/session/*", EventStreamServlet::class.java)
        context.servletManager.addServlet("/history", TagHistoryServlet::class.java)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Tag Stream module.")
        context.eventStreamManager.closeAllSessions()
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