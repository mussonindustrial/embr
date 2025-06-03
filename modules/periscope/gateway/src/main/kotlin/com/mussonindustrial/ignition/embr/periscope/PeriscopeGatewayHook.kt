package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.mussonindustrial.ignition.embr.periscope.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.periscope.handlers.ClientResourceHandler
import com.mussonindustrial.ignition.embr.periscope.handlers.ClientResourceManifestHandler
import com.mussonindustrial.ignition.embr.periscope.handlers.SystemModuleHandler
import com.mussonindustrial.ignition.embr.periscope.scripting.JavaScriptFunctions
import com.mussonindustrial.ignition.embr.periscope.scripting.QueueFunctions
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class PeriscopeGatewayHook : AbstractGatewayModuleHook() {

    private val logger: Logger = LoggerFactory.getLogger(SHORT_MODULE_ID)
    private lateinit var context: PeriscopeGatewayContext

    override fun setup(context: GatewayContext) {
        logger.debug("Embr-Periscope module setup.")
        this.context = PeriscopeGatewayContext(context)
        BundleUtil.get().addBundle(Meta.BUNDLE_PREFIX, this::class.java.classLoader, "localization")
    }

    override fun startup(activationState: LicenseState) {
        logger.debug("Embr-Periscope module startup.")

        logger.debug("Injecting required resources...")
        context.injectResources()

        logger.debug("Registering components...")
        context.registerComponents()

        logger.debug("Registering ClientResource definitions...")
        context.registerClientResourceDefinitions()

        logger.debug("Starting project lifecycles...")
        context.startupProjectLifecycles()
    }

    override fun shutdown() {
        logger.debug("Embr-Periscope module shutdown.")
        BundleUtil.get().removeBundle(Meta.BUNDLE_PREFIX)

        logger.debug("Removing injected resources...")
        context.removeResources()

        logger.debug("Removing components...")
        context.removeComponents()

        logger.debug("Removing servlets...")
        context.removeServlets()

        logger.debug("Stopping project lifecycles...")
        context.shutdownProjectLifecycles()
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

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule(
            "system.perspective",
            JavaScriptFunctions(this.context),
            PropertiesFileDocProvider(),
        )
        manager.addScriptModule(
            "system.perspective",
            QueueFunctions(this.context),
            PropertiesFileDocProvider(),
        )
        manager.addScriptModule(
            "system.perspective",
            QueueFunctions(this.context),
            PropertiesFileDocProvider(),
        )
    }

    override fun mountRouteHandlers(routes: RouteGroup) {
        ClientResourceManifestHandler(context)
            .mount(routes.newRoute("/client-resource/:project_name/manifest.json"))
        SystemModuleHandler(context).mount(routes.newRoute("/system-module/:hash/:module_name"))
        ClientResourceHandler(context)
            .mount(routes.newRoute("/client-resource/:project_name/:hash/*"))
    }
}
