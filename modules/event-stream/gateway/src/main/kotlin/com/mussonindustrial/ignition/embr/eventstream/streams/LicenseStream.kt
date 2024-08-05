package com.mussonindustrial.ignition.embr.eventstream.streams

import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.gateway.licensing.LicenseStateUpdateEvent
import com.inductiveautomation.ignition.gateway.licensing.LicenseStateUpdateListener
import com.mussonindustrial.ignition.embr.eventstream.EventStreamGatewayContext

class LicenseStream : EventStream, LicenseStateUpdateListener {
    override val key = Companion.key

    companion object : EventStreamCompanion<LicenseStream> {
        override val key = "license"

        override fun get(): LicenseStream {
            return LicenseStream()
        }
    }

    private val context = EventStreamGatewayContext.instance
    lateinit var session: EventStreamManager.Session

    override fun initialize(props: JsonElement) { }

    override fun onCreate(session: EventStreamManager.Session) {
        this.session = session
    }

    override fun onOpen() {
        context.licenseManager.addPlatformLicenseStateUpdateListener(this)
        context.licenseManager.addLicenseStateUpdateListener(this)
    }

    override fun onClose() {
        context.licenseManager.removePlatformLicenseStateUpdateListener(this)
        context.licenseManager.removeLicenseStateUpdateListener(this)
    }

    override fun licenseStateUpdated(event: LicenseStateUpdateEvent) {
        val json =
            JsonObject().apply {
                addProperty("module_id", event.moduleId)
                add(
                    "license_state",
                    JsonObject().apply {
                        addProperty("mode", event.licenseState.licenseMode.name)
                        addProperty("is_trial_expired", event.licenseState.isTrialExpired)
                        addProperty("trial_expiration_date", event.licenseState.trialExpirationDate.time)
                    },
                )
            }
        session.emitEvent("license_change", json.toString())
    }

    override fun toGson(): JsonElement {
        return JsonObject()
    }
}
