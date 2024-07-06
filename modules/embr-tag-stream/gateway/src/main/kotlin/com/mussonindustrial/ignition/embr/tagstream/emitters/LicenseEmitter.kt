package com.mussonindustrial.ignition.embr.tagstream.emitters

import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.gateway.licensing.LicenseStateUpdateEvent
import com.inductiveautomation.ignition.gateway.licensing.LicenseStateUpdateListener
import com.mussonindustrial.ignition.embr.tagstream.EventStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.EventStreamManager
import kotlin.jvm.optionals.getOrNull

class LicenseEmitter : EventEmitter, LicenseStateUpdateListener {

    override val key = KEY
    companion object {
        const val KEY = "license"
        val gsonAdapter = JsonSerializer<LicenseEmitter> { eventEmitter, _, _ ->
            eventEmitter.gson.toJsonTree(eventEmitter)
        }
    }

    private val context = EventStreamGatewayContext.INSTANCE
    lateinit var session: EventStreamManager.Session
    override fun initialize(props: JsonElement) { }

    override fun onCreation(session: EventStreamManager.Session) {
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
        val json = JsonObject().apply {
            addProperty("module_id", event.moduleId)
            add("license_state", JsonObject().apply {
                addProperty("mode", event.licenseState.licenseMode.name)
                addProperty("is_trial_expired", event.licenseState.isTrialExpired)
                addProperty("trial_expiration_date", event.licenseState.trialExpirationDate.time)
                add("module_license", JsonObject().apply {
                    addProperty("version", event.licenseState.moduleLicense.version)
                    addProperty("expiration_date", event.licenseState.moduleLicense.expirationDate.getOrNull()?.time ?: 0)

                })
            })
        }
        session.emitEvent("license_change", json.toString())
    }

    private val gsonAdapter = JsonSerializer<LicenseEmitter> { _, _, _ ->
        JsonObject()
    }
    private val gson: Gson = GsonBuilder().apply {
        registerTypeAdapter(LicenseEmitter::class.java, gsonAdapter)
    }.create()
}