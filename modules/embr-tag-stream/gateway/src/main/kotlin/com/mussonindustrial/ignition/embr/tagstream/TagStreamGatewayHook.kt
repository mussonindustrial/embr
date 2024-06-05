package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.tagstream.Meta.URL_ALIAS
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamSubscriptionServlet
import java.util.*


@Suppress("unused")
class TagStreamGatewayHook : AbstractGatewayModuleHook() {

    companion object {
        lateinit var context: TagStreamGatewayContext
    }

    private val logger = this.getLogger()
    private lateinit var context: TagStreamGatewayContext

    override fun setup(context: GatewayContext) {
        this.context = TagStreamGatewayContext(context)
        Companion.context = this.context
    }


    override fun startup(activationState: LicenseState) {
        logger.info("Embr Tag Stream module started.")
        context.servletManager.addServlet("/stream/*", TagStreamServlet::class.java)
        context.servletManager.addServlet("/subscribe", TagStreamSubscriptionServlet::class.java)
    }

    override fun shutdown() {
        logger.info("Shutting down Embr Tag Stream module.")
        context.servletManager.removeAllServlets()
        context.tagStreamManager.closeAllStreams()

    }

    override fun getMountedResourceFolder(): Optional<String> {
        return Optional.of("static")
    }

    override fun getMountPathAlias(): Optional<String> {
        return Optional.of(URL_ALIAS)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}