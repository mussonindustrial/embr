package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.gson.JsonArray
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
import com.mussonindustrial.ignition.embr.periscope.model.subscribeToParams
import com.mussonindustrial.ignition.embr.periscope.model.writeToParams
import java.util.*
import java.util.concurrent.TimeUnit

class FlexRepeater2ViewModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "FlexRepeaterModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val viewLoader = context.getViewLoader(component.page as PageModel)
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>>()

    private val commonViewPathListener = createCommonViewPathListener()
    private val commonViewParamsListener = createCommonViewParamsListener()
    private val viewPathListeners = createViewPathListeners()
    private val viewParamsListeners = createViewParamsListeners()
    private val viewTimeoutMs = 10_000L

    private var instanceCount = 0

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            props.instances.forEach { instance -> initializeView(instance) }
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debugf("Shutdown")
            viewPathListeners.onEach { it.value.unsubscribe() }
            viewParamsListeners.onEach { it.value.unsubscribe() }
            commonViewPathListener.unsubscribe()
            commonViewParamsListener.unsubscribe()
            shutdownViewOutputListeners()
        }
    }

    private fun createViewOutputListeners(
        instance: InstancePropsHandler,
        viewModel: ViewModel
    ): Map<String, Subscription>? {
        return viewModel.subscribeToParams(Origin.allBut(Origin.Delegate)) {
            this.onViewOutputChanged(instance, it)
        }
    }

    private fun shutdownViewOutputListeners() {
        component.mdc {
            log.tracef("Removing view output listeners.")

            viewOutputListeners.forEach { listenerSet ->
                listenerSet.value?.forEach { it.value.unsubscribe() }
            }
            viewOutputListeners.clear()
        }
    }

    private fun shutdownViewOutputListeners(viewModel: ViewModel) {
        component.mdc {
            log.tracef("Removing view output listeners.")

            viewOutputListeners[viewModel]?.forEach { it.value.unsubscribe() }
            viewOutputListeners.clear()
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        try {
            component.mdcSetup()
            log.debugf("Received {} component message.", message.eventName)

            if (message.eventName == ViewJoinMsg.PROTOCOL) {
                val event = ViewJoinMsg(message.event)
                val instance = props.instances[event.index]
                if (instance == null) {
                    component.mdc { log.warnf("Client requested view for invalid instance.") }
                    return
                }

                if (
                    event.resourcePath != instance.viewPath || event.mountPath != instance.mountPath
                ) {
                    component.mdc {
                        log.warnf("Client requested unexpected resource or mount path.")
                    }
                    return
                }

                log.tracef("Client is requesting to join view {}", event.instanceId().id)

                viewLoader
                    .findOrStartView(
                        event.resourcePath,
                        event.mountPath,
                        event.birthDate,
                        instance.viewParams
                    )
                    .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
                    .thenAccept {
                        if (it.isPresent) {
                            initializeView(instance, it.get())
                        }
                    }
            }
        } finally {
            component.mdcTeardown()
        }
    }

    private fun createCommonViewPathListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewPath", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance -> initializeView(instance) }
        }
    }

    private fun createCommonViewParamsListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewParams", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance -> onViewInputChanged(instance, it) }
        }
    }

    private fun createInstancesListener(): Subscription {
        return props.tree.subscribe("instances", Origin.allBut(Origin.Delegate)) { event ->
            val instances = toJsonDeep(event.readValue()).asJsonArray ?: JsonArray()
            val newInstanceCount = instances.size()
            if (newInstanceCount == instanceCount) {
                return@subscribe
            }
            instanceCount = newInstanceCount

            props.instances.forEach {
                if (viewPathListeners[it.mountPath] == null) {
                    viewPathListeners[it.mountPath] = createViewPathListener(it)
                }
                if (viewParamsListeners[it.mountPath] == null) {
                    viewParamsListeners[it.mountPath] = createViewParamsListener(it)
                }
                initializeView(it)
            }
        }
    }

    private fun createViewPathListener(instance: InstancePropsHandler): Subscription {
        return props.tree.subscribe(
            "instance[${instance.index}].viewPath",
            Origin.allBut(Origin.Delegate)
        ) {
            initializeView(instance)
        }
    }

    private fun createViewPathListeners(): MutableMap<String, Subscription> {
        return props.instances
            .associate { it.mountPath to createViewPathListener(it) }
            .toMutableMap()
    }

    private fun createViewParamsListener(instance: InstancePropsHandler): Subscription {
        return props.tree.subscribe(
            "instance[${instance.index}].viewParams",
            Origin.allBut(Origin.Delegate)
        ) {
            onViewInputChanged(instance, it)
        }
    }

    private fun createViewParamsListeners(): MutableMap<String, Subscription> {
        return props.instances
            .associate { it.mountPath to createViewParamsListener(it) }
            .toMutableMap()
    }

    private fun initializeView(instance: InstancePropsHandler) =
        onView(instance) { viewModel -> initializeView(instance, viewModel) }

    private fun initializeView(instance: InstancePropsHandler, viewModel: ViewModel) {
        if (viewOutputListeners[viewModel] == null) {
            viewModel.writeToParams(instance.viewParams, Origin.Delegate, this, queue)
        }

        viewOutputListeners[viewModel]?.apply { shutdownViewOutputListeners(viewModel) }
        viewOutputListeners[viewModel] = createViewOutputListeners(instance, viewModel)
    }

    private fun onViewInputChanged(instance: InstancePropsHandler, event: PropertyTreeChangeEvent) {
        if (event.source == this) {
            return
        }

        onView(instance) { viewModel ->
            val path = event.path.toString().replace("${instance.treePath}.viewParams.", "")
            val value =
                toJsonDeep(event.readCausalValue(), BindingUtils.JsonEncoding.DollarQualified)
            viewModel.writeToParams(path, value, Origin.Delegate, this, queue)
        }
    }

    private fun onViewOutputChanged(
        instance: InstancePropsHandler,
        event: PropertyTreeChangeEvent
    ) {
        if (event.source === this) {
            return
        }

        val newValue =
            toJsonDeep(event.readValue().value, BindingUtils.JsonEncoding.DollarQualified)

        val path = "${instance.treePath}.viewParams.${event.listeningPath}"
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

    private fun onView(instance: InstancePropsHandler, block: (ViewModel) -> Unit) {
        val resourcePath = instance.viewPath
        val mountPath = instance.mountPath
        val viewParams = instance.viewParams

        viewLoader
            .findOrStartView(resourcePath, mountPath, Date().time, viewParams)
            .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
            .thenAccept { maybeViewModel ->
                if (maybeViewModel.isEmpty) {
                    component.mdc { log.warnf("Failed to find view to operate on: $resourcePath") }
                } else {
                    block(maybeViewModel.get())
                }
            }
    }

    inner class InstancePropsHandler(val tree: PropertyTree, val index: Int) {

        val treePath = "instance[$index]"

        private val commonViewPath: String
            get() {
                return toJsonDeep(
                        tree.read("instanceCommon.viewPath").orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        private val instanceViewPath: String
            get() {
                return toJsonDeep(
                        tree.read("instance[$index].viewPath").orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        private val commonViewParams: JsonObject
            get() {
                return toJsonDeep(
                        tree
                            .read("instanceCommon.viewParams")
                            .orElse(BasicQualifiedValue(JsonObject())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonObject ?: JsonObject()
            }

        private val instanceViewParams: JsonObject
            get() {
                return toJsonDeep(
                        tree
                            .read("instance[$index].viewParams")
                            .orElse(BasicQualifiedValue(JsonObject())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonObject ?: JsonObject()
            }

        val viewPath: String
            get() {
                val path = instanceViewPath
                if (path == "") {
                    return commonViewPath
                }
                return path
            }

        val viewParams: JsonObject
            get() {
                return JsonObject().apply {
                    commonViewParams.entrySet()?.forEach { add(it.key, it.value) }
                    instanceViewParams.entrySet()?.forEach { add(it.key, it.value) }
                }
            }

        val key: String
            get() {
                return toJsonDeep(
                        tree.read("instance[$index].key").orElse(BasicQualifiedValue(index))
                    )
                    ?.asString ?: ""
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}$${component.componentAddressPath}.$key"
            }
    }

    inner class InstancesPropsHandlers(private val tree: PropertyTree) :
        Iterable<InstancePropsHandler> {
        val json: JsonArray
            get() {
                return toJsonDeep(
                        tree.read("instances").orElse(BasicQualifiedValue(JsonArray())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonArray ?: JsonArray()
            }

        private val size: Int
            get() = json.size()

        operator fun get(index: Int): InstancePropsHandler? {
            if (index in 0..size) {
                return InstancePropsHandler(tree, index)
            }
            return null
        }

        override operator fun iterator() = iterator {
            for (index in 0..size) {
                this.yield(InstancePropsHandler(tree, index))
            }
        }
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val instances = InstancesPropsHandlers(tree)
    }

    class ViewJoinMsg(event: JsonObject) {
        companion object {
            const val PROTOCOL: String = "view-join"
        }

        val resourcePath: String = event.get("resourcePath")?.asString ?: ""
        val mountPath: String = event.get("mountPath")?.asString ?: ""
        val birthDate: Long = event.get("birthDate")?.asLong ?: 0
        val params: JsonObject = event.get("params")?.asJsonObject ?: JsonObject()
        val index: Int = event.get("index")?.asInt ?: 0

        fun instanceId(): ViewInstanceId {
            return ViewInstanceId(this.resourcePath, this.mountPath)
        }
    }
}
