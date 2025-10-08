package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.config.ViewConfig
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.*
import com.inductiveautomation.perspective.gateway.binding.BindingUtils
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent
import com.mussonindustrial.embr.perspective.gateway.model.subscribeToParams
import com.mussonindustrial.embr.perspective.gateway.model.writeToParams
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.api.ViewJoinMsg
import java.util.*

class JsonViewModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val context = PeriscopeGatewayContext.instance
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val viewLoader = context.getViewLoader(component.page as PageModel)
    private val gson = context.perspectiveContext.sharedGson

    private var viewModel: ViewModel? = null
    private val viewParamsListener = createViewParamsListener()
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>>()

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            initializeView()
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debugf("Shutdown")
            viewParamsListener.unsubscribe()
            shutdownViewOutputListeners()
        }
    }

    private fun createViewOutputListeners(viewModel: ViewModel): Map<String, Subscription>? {
        return viewModel.subscribeToParams(Origin.allBut(Origin.Delegate)) {
            this.onViewOutputChanged(it)
        }
    }

    private fun shutdownViewOutputListeners() {
        viewOutputListeners.keys.forEach { shutdownViewOutputListeners(it) }
    }

    private fun shutdownViewOutputListeners(viewModel: ViewModel) {
        viewOutputListeners[viewModel]?.apply {
            component.mdc {
                log.tracef("Removing output listeners for view %s", viewModel.id.mountPath)
                forEach { it.value.unsubscribe() }
                viewOutputListeners[viewModel] = null
            }
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        try {
            component.mdcSetup()
            log.tracef("Received '%s' component message.", message.eventName)

            if (message.eventName == ViewJoinMsg.PROTOCOL) {
                initializeView()
            }
        } finally {
            component.mdcTeardown()
        }
    }

    private fun initializeView() {
        if (props.viewConfig.root == null) {
            log.debugf("No root component for view.")
            return
        }

        val page = (component.page as PageModel)
        val viewId = ViewInstanceId(props.resourcePath, props.mountPath)

        val newViewModel =
            component.session.createViewModel(page, viewId, props.viewConfig, props.viewParams)
        newViewModel.birthDate = Date().time

        viewLoader.addView(viewId, newViewModel)
        newViewModel.startup()

        if (viewOutputListeners[newViewModel] == null) {
            newViewModel.writeToParams(props.viewParams, Origin.Delegate, this)
        }

        viewOutputListeners[newViewModel]?.apply { shutdownViewOutputListeners(newViewModel) }
        viewOutputListeners[newViewModel] = createViewOutputListeners(newViewModel)

        viewModel = newViewModel
    }

    private fun createViewParamsListener(): Subscription {
        return props.tree.subscribe("viewParams", Origin.allBut(Origin.Delegate)) {
            onViewInputChanged(it)
        }
    }

    private fun onViewInputChanged(event: PropertyTreeChangeEvent) {
        if (event.source == this) {
            return
        }

        onView { viewModel ->
            val path = event.path.toString().replace("viewParams.", "")
            val value =
                toJsonDeep(event.readCausalValue(), BindingUtils.JsonEncoding.DollarQualified)
            viewModel.writeToParams(path, value, Origin.Delegate, this)
        }
    }

    private fun onViewOutputChanged(event: PropertyTreeChangeEvent) {
        if (event.source === this) {
            return
        }

        val newValue =
            toJsonDeep(event.readValue().value, BindingUtils.JsonEncoding.DollarQualified)

        val path = "viewParams.${event.listeningPath}"
        var currentValue: Any? = null

        val maybeCurrentValue = props.tree.read(path)
        if (maybeCurrentValue.isPresent) {
            currentValue =
                toJsonDeep(maybeCurrentValue.get(), BindingUtils.JsonEncoding.DollarQualified)
        }

        if (currentValue == newValue) {
            return
        }

        props.tree.write(path, newValue, Origin.BindingWriteback, this)
    }

    private fun onView(block: (ViewModel) -> Unit) {
        if (viewModel == null) {
            initializeView()
        }
        block(viewModel!!)
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val viewJson: JsonObject
            get() {
                val viewJson = tree.read("viewJson")
                if (viewJson.isEmpty) {
                    return JsonObject()
                }

                return toJsonDeep(viewJson.get()).asJsonObject
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

        val resourcePath: String
            get() {
                return "${component.view?.id?.resourcePath}.${component.componentAddressPath}"
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}.${component.componentAddressPath}"
            }
    }
}
