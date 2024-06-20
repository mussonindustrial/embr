package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.tagstream.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamServlet
import com.mussonindustrial.ignition.embr.tagstream.servlets.TagStreamManagerServlet
import java.util.*


@Suppress("unused")
class TagStreamGatewayHook : AbstractGatewayModuleHook() {

    companion object {
        lateinit var context: TagStreamGatewayContext
    }

    private val logger = this.getLogger()

    override fun setup(context: GatewayContext) {
        Companion.context = TagStreamGatewayContext(context)
    }


    override fun startup(activationState: LicenseState) {
        logger.info("Embr Tag Stream module started.")
        context.servletManager.addServlet("/session", TagStreamManagerServlet::class.java)
        context.servletManager.addServlet("/session/*", TagStreamServlet::class.java)
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
        return Optional.of(SHORT_MODULE_ID)
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}