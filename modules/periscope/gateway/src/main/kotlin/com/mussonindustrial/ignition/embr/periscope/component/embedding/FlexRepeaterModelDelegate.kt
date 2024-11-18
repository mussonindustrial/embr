package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
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
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.model.subscribeToParams
import com.mussonindustrial.ignition.embr.periscope.model.writeToParams
import com.mussonindustrial.ignition.embr.periscope.page.ViewJoinMsg
import java.util.*
import java.util.concurrent.TimeUnit
import org.python.core.PyObject

class FlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "FlexRepeaterModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val methods = PyArgOverloads()
    private val viewLoader = context.getViewLoader(component.page as PageModel)
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>>()

    private val instancesListener = createInstancesListener()
    private val commonViewPathListener = createCommonViewPathListener()
    private val commonViewParamsListener = createCommonViewParamsListener()
    private val viewPathListeners = createViewPathListeners()
    private val viewParamsListeners = createViewParamsListeners()
    private val viewTimeoutMs = 10_000L

    private var instanceCount = 0

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            props.instances.forEach { instance ->
                initializeView(instance)
                log.info(instance.mountPath)
            }
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debugf("Shutdown")
            instancesListener.unsubscribe()
            commonViewPathListener.unsubscribe()
            commonViewParamsListener.unsubscribe()
            viewPathListeners.onEach { it.value.unsubscribe() }
            viewParamsListeners.onEach { it.value.unsubscribe() }
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
            log.debugf("Received %s component message.", message.eventName)

            if (message.eventName == ViewJoinMsg.PROTOCOL) {
                val event = ViewJoinMsg(message.event)

                val instance = findInstance(event.mountPath)
                if (instance == null) {
                    component.mdc {
                        log.warnf(
                            "Client requested view for invalid instance [%s].",
                            event.mountPath
                        )
                    }
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

                log.tracef("Client is requesting to join view %s", event.instanceId().id)

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
            "${instance.treePath}.viewPath",
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
            "${instance.treePath}.viewParams",
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

    private fun updateInstances(newInstances: JsonArray, forceWrite: Boolean?) {
        var updateCount = 0

        newInstances.forEachIndexed { index, instance ->
            val viewParams = instance.asJsonObject?.getAsJsonObject("viewParams") ?: JsonObject()

            updateCount += updateKey(instance.asJsonObject)
            updateCount += updateIndex(index, viewParams)

            instance.asJsonObject.add("viewParams", viewParams)
        }

        if (updateCount > 0 || forceWrite == true) {
            if (log.isTraceEnabled) {
                component.mdc { log.trace("Performing instance repair: $newInstances") }
            }

            props.instances.json = newInstances
        }
    }

    private fun updateKey(instance: JsonObject): Int {
        val currentKey = instance.get("key")
        if (currentKey == null || currentKey.toString() == "") {
            instance.addProperty("key", UUID.randomUUID().toString())
            return 1
        }
        return 0
    }

    private fun updateIndex(index: Int, viewParams: JsonObject): Int {
        val currentIndex = viewParams.get("index")
        if (currentIndex == null || currentIndex.toString() != index.toString()) {
            viewParams.addProperty("index", index)
            return 1
        }
        return 0
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

    private fun findInstance(mountPath: String): InstancePropsHandler? {
        return props.instances.firstOrNull { it.mountPath == mountPath }
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

    inner class InstancePropsHandler(private val tree: PropertyTree, val index: Int) {

        val treePath = "instances[$index]"

        private val commonViewPath: String
            get() {
                return toJsonDeep(
                        tree.read("instanceCommon.viewPath").orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        private val instanceViewPath: String
            get() {
                return toJsonDeep(tree.read("$treePath.viewPath").orElse(BasicQualifiedValue("")))
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
                        tree.read("$treePath.viewParams").orElse(BasicQualifiedValue(JsonObject())),
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
                return toJsonDeep(tree.read("$treePath.key").orElse(BasicQualifiedValue(index)))
                    ?.asString ?: ""
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}$${component.componentAddressPath}.$key"
            }
    }

    inner class InstancesPropsHandlers(private val tree: PropertyTree) :
        Iterable<InstancePropsHandler> {
        var json: JsonArray
            get() {
                return toJsonDeep(
                        tree.read("instances").orElse(BasicQualifiedValue(JsonArray())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonArray ?: JsonArray()
            }
            set(value) {
                tree.write("instances", value, Origin.Delegate, this)
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

    @ScriptCallable
    @KeywordArgs(
        names = ["index"],
        types = [Int::class],
    )
    fun popInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.popInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["instance"],
        types = [PyObject::class],
    )
    fun pushInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.pushInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["index", "instance"],
        types = [Int::class, PyObject::class],
    )
    fun insertInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.insertInstance.call(args, keywords) } }

    inner class PyArgOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = props.instances.json
                        newInstances.remove(it[0] as Int)
                        updateInstances(newInstances, true)
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val newInstances = props.instances.json
                    newInstances.remove(newInstances.size() - 1)
                    updateInstances(newInstances, true)
                })
                .build()

        val pushInstance =
            PyArgOverloadBuilder()
                .setName("pushInstance")
                .addOverload(
                    {
                        val instance = TypeUtilities.pyToGson(it[0] as PyObject?)
                        val newInstances = props.instances.json

                        if (instance.isJsonObject) {
                            newInstances.add(instance)
                        } else if (instance.isJsonArray) {
                            newInstances.addAll(instance.asJsonArray)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        queue.submit { updateInstances(newInstances, true) }
                    },
                    "instance" to PyObject::class,
                )
                .build()

        val insertInstance =
            PyArgOverloadBuilder()
                .setName("insertInstance")
                .addOverload(
                    {
                        val index = it[0] as Int
                        val instance = TypeUtilities.pyToGson(it[1] as PyObject?)

                        val existingInstances = props.instances.json
                        val newInstances =
                            JsonArray().apply {
                                existingInstances.take(index).forEach { instance -> add(instance) }
                            }

                        if (instance.isJsonObject) {
                            newInstances.add(instance)
                        } else if (instance.isJsonArray) {
                            newInstances.addAll(instance.asJsonArray)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        newInstances.apply {
                            existingInstances.drop(index).forEach { instance -> add(instance) }
                        }

                        queue.submit { updateInstances(newInstances, true) }
                    },
                    "index" to Int::class,
                    "instance" to PyObject::class
                )
                .build()
    }
}
