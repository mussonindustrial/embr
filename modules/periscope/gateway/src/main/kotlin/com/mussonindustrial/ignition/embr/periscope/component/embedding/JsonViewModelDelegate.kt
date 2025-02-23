package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.config.ViewConfig
import com.inductiveautomation.perspective.gateway.api.*
import com.inductiveautomation.perspective.gateway.binding.BindingUtils
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.api.ViewJoinMsg

class JsonViewModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "JsonViewModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val viewLoader = context.getViewLoader(component.page as PageModel)
    private val gson = context.perspectiveContext.sharedGson

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
    }

    override fun handleEvent(message: EventFiredMsg) {
        try {
            component.mdcSetup()
            log.tracef("Received '%s' component message.", message.eventName)

            if (message.eventName == ViewJoinMsg.PROTOCOL) {
                initializeView(ViewJoinMsg(message.event))
            }
        } finally {
            component.mdcTeardown()
        }
    }

    private fun initializeView(message: ViewJoinMsg) {
        val page = (component.page as PageModel)
        val viewId = ViewInstanceId(message.resourcePath, message.mountPath)

        val model =
            component.session.createViewModel(page, viewId, props.viewConfig, props.viewParams)
        model.birthDate = message.birthDate

        viewLoader.addView(viewId, model)
        log.info("Model isRunning: ${model.isRunning}")
        model.startup()
        log.info("Model isRunning: ${model.isRunning}")
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val viewJson: JsonObject
            get() {
                val viewPath = tree.read("viewJson")
                if (viewPath.isEmpty) {
                    return JsonObject()
                }

                return toJsonDeep(viewPath.get()).asJsonObject
            }

        val viewConfig: ViewConfig
            get() {
                return gson.fromJson(this.viewJson, ViewConfig::class.java)
            }

        val viewParams: JsonObject
            get() {
                val viewParams = tree.read("viewParams")
                if (viewParams.isEmpty) {
                    return JsonObject()
                }

                return toJsonDeep(viewParams.get(), BindingUtils.JsonEncoding.DollarQualified)
                    .asJsonObject
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}.${component.componentAddressPath}"
            }
    }
}
