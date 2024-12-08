package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.common.api.PropertyType
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
import java.util.concurrent.TimeUnit

class EmbeddedViewModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "EmbeddedViewModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val viewLoader = context.getViewLoader(component.page as PageModel)

    private val viewPathListener = createViewPathListener()
    private val viewParamsListener = createViewParamsListener()
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>>()
    private val viewTimeoutMs = 10_000L

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            initializeView()
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debugf("Shutdown")
            viewPathListener.unsubscribe()
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
                joinView(ViewJoinMsg(message.event))
            }
        } finally {
            component.mdcTeardown()
        }
    }

    private fun joinView(event: ViewJoinMsg) {
        if (event.resourcePath != props.viewPath || event.mountPath != props.mountPath) {
            component.mdc { log.warnf("Client requested unexpected resource or mount path.") }
            return
        }

        log.tracef("Client is requesting to join view %s", event.mountPath)

        viewLoader
            .findOrStartView(event.resourcePath, event.mountPath, event.birthDate, props.viewParams)
            .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
            .thenAcceptAsync(
                {
                    if (it.isPresent) {
                        initializeView(it.get(), true)
                    }
                },
                queue::submit
            )
    }

    private fun createViewPathListener(): Subscription {
        return props.tree.subscribe("viewPath", Origin.allBut(Origin.Delegate)) { initializeView() }
    }

    private fun createViewParamsListener(): Subscription {
        return props.tree.subscribe("viewParams", Origin.allBut(Origin.Delegate)) {
            onViewInputChanged(it)
        }
    }

    private fun initializeView() = onView { viewModel -> initializeView(viewModel) }

    private fun initializeView(viewModel: ViewModel) {
        initializeView(viewModel, forceWrite = false)
    }

    private fun initializeView(viewModel: ViewModel, forceWrite: Boolean) {
        if (viewOutputListeners[viewModel] == null || forceWrite) {
            viewModel.writeToParams(props.viewParams, Origin.Delegate, this)
        }

        viewOutputListeners[viewModel]?.apply { shutdownViewOutputListeners(viewModel) }
        viewOutputListeners[viewModel] = createViewOutputListeners(viewModel)
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
        val resourcePath = props.viewPath
        val mountPath = props.mountPath
        val viewParams = props.viewParams

        viewLoader
            .findOrStartView(resourcePath, mountPath, Date().time, viewParams)
            .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
            .thenAcceptAsync(
                { maybeViewModel ->
                    if (maybeViewModel.isEmpty) {
                        component.mdc {
                            log.warnf("Failed to find view '%s' to operate on", resourcePath)
                        }
                    } else {
                        block(maybeViewModel.get())
                    }
                },
                queue::submit
            )
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val viewPath: String
            get() {
                val viewPath = tree.read("viewPath")
                if (viewPath.isEmpty) {
                    return ""
                }

                return toJsonDeep(viewPath.get()).asString
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
