package com.mussonindustrial.embr.snmp

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.opcua.server.api.AbstractDeviceModuleHook
import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceType
import com.mussonindustrial.embr.common.Embr
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class SnmpGatewayHook : AbstractDeviceModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(Embr.SNMP.shortId)

    private val snmpContext: SnmpGatewayContext
        get() = this.context as SnmpGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-SNMP module setup.")
        BundleUtil.get().addBundle("Snmp", this::class.java.classLoader, "localization")

        super.setup(SnmpGatewayContext(context))

        snmpContext.updatePersistentRecords()
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-SNMP module startup.")
        super.startup(activationState)
    }

    override fun shutdown() {
        logger.debug("Embr-SNMP module shutdown.")
        BundleUtil.get().removeBundle("Snmp")

        super.shutdown()
    }

    override fun getDeviceTypes(): List<DeviceType> {
        return snmpContext.deviceTypes
    }

    override fun isFreeModule(): Boolean {
        return true
    }

    override fun isMakerEditionCompatible(): Boolean {
        return true
    }
}
