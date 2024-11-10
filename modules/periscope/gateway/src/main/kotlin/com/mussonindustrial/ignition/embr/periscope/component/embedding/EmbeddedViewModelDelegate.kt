package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
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
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.page.ViewJoinMsg
import java.util.*
import java.util.concurrent.TimeUnit

class EmbeddedViewModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "EmbeddedViewModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val viewLoader = context.getViewLoader(component.page as PageModel)
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>?>()
    private val viewPathListener = createViewPathListener()
    private val viewParamsListener = createViewParamsListener()
    private val viewTimeoutMs = 10_000L

    override fun onStartup() {
        component.mdc {
            log.debug("Startup")
            queue.submit { initializeView() }
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debug("Shutdown")
            viewPathListener.unsubscribe()
            viewParamsListener.unsubscribe()
            shutdownViewOutputListeners()
        }
    }

    private fun shutdownViewOutputListeners() {
        log.debug("Removing view output listeners.")
        viewOutputListeners.forEach { listenerSet ->
            listenerSet.value?.forEach { it.value.unsubscribe() }
        }
        viewOutputListeners.clear()
    }

    override fun handleEvent(message: EventFiredMsg) {
        if (message.eventName == ViewJoinMsg.PROTOCOL) {
            val event = ViewJoinMsg(message.event)

            if (event.resourcePath != props.viewPath || event.mountPath != props.mountPath) {
                component.mdc { log.warn("Client requested unexpected resource or mount path.") }
            }

            viewLoader
                .findOrStartView(
                    event.resourcePath,
                    event.mountPath,
                    event.birthDate,
                    props.viewParams
                )
                .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
                .thenAccept {
                    if (it.isPresent) {
                        initializeView(it.get())
                    }
                }
        }
    }

    private fun createViewPathListener(): Subscription {
        return props.tree.subscribe("viewPath", Origin.allBut(Origin.Delegate)) {
            queue.submit { initializeView() }
        }
    }

    private fun createViewParamsListener(): Subscription {
        return props.tree.subscribe("viewParams", Origin.allBut(Origin.Delegate)) {
            queue.submit { onViewInputChanged(it) }
        }
    }

    private fun initializeView() = onView { viewModel -> initializeView(viewModel) }

    private fun initializeView(viewModel: ViewModel) {
        viewModel.writeToParams(props.viewParams, Origin.Delegate, this, queue)

        if (viewOutputListeners[viewModel] == null) {
            val listeners =
                viewModel.subscribeToParams(Origin.allBut(Origin.Delegate)) {
                    this.onViewOutputChanged(it)
                }
            viewOutputListeners[viewModel] = listeners
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
            viewModel.writeToParams(path, value, Origin.Delegate, this, queue)
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
            .thenAccept { maybeViewModel ->
                if (maybeViewModel.isEmpty) {
                    component.mdc { log.warn("Failed to find view to operate on: $resourcePath") }
                } else {
                    block(maybeViewModel.get())
                }
            }
    }

    inner class PropsHandler(val tree: PropertyTree) {
        var viewPath: String
            get() {
                val instancesProp = tree.read("viewPath")
                if (instancesProp.isEmpty) {
                    return ""
                }

                return toJsonDeep(instancesProp.get()).asString
            }
            set(value) {
                tree.write("viewPath", BasicQualifiedValue(value), Origin.Delegate, this)
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
