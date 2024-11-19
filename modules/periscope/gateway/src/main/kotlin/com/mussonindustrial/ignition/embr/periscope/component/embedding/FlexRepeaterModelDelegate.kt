package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.JsonPath
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
import com.mussonindustrial.ignition.embr.periscope.model.readParams
import com.mussonindustrial.ignition.embr.periscope.model.subscribeToParams
import com.mussonindustrial.ignition.embr.periscope.model.writeToParams
import com.mussonindustrial.ignition.embr.periscope.page.ViewJoinMsg
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.drop
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.take
import org.python.core.PyObject

class FlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "FlexRepeaterModelDelegate")
    private val context = PeriscopeGatewayContext.instance
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val methods = MethodOverloads()
    private val viewLoader = context.getViewLoader(component.page as PageModel)

    private val instancesListener: Subscription = createInstancesListener()
    private val commonViewPathListener: Subscription = createCommonViewPathListener()
    private val commonViewParamsListener: Subscription = createCommonViewParamsListener()
    private val viewOutputListeners = WeakHashMap<ViewModel, Map<String, Subscription>>()
    private val viewTimeoutMs = 10_000L

    private var instanceCount = 0
    private val instanceMatch = Regex("instances\\[(\\d+)]\\.(.*+)")

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            props.instances.forEach { it.initialize(forceWrite = false) }
        }
    }

    override fun onShutdown() {
        component.mdc {
            log.debugf("Shutdown")
            instancesListener.unsubscribe()
            commonViewPathListener.unsubscribe()
            commonViewParamsListener.unsubscribe()
            shutdownViewOutputListeners()
        }
    }

    private fun shutdownViewOutputListeners() {
        component.mdc {
            viewOutputListeners.forEach { (_, params) ->
                params?.forEach { (_, subscription) -> subscription.unsubscribe() }
            }
            viewOutputListeners.clear()
        }
    }

    private fun shutdownViewOutputListeners(viewModel: ViewModel) {
        component.mdc {
            log.tracef("Removing view output listeners for view [%s]", viewModel.id.mountPath)
            viewOutputListeners[viewModel]?.forEach { (_, subscription) ->
                subscription.unsubscribe()
            }
            viewOutputListeners.clear()
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        try {
            component.mdcSetup()
            log.debugf("Received %s component message.", message.eventName)

            if (message.eventName == ViewJoinMsg.PROTOCOL) {
                joinView(ViewJoinMsg(message.event))
            }
        } finally {
            component.mdcTeardown()
        }
    }

    private fun joinView(event: ViewJoinMsg) {
        val instance = props.instances.find(event.mountPath)
        if (instance == null) {
            component.mdc {
                log.warnf("Client requested view for invalid instance [%s].", event.mountPath)
            }
            return
        }

        if (event.resourcePath != instance.viewPath || event.mountPath != instance.mountPath) {
            component.mdc { log.warnf("Client requested unexpected resource or mount path.") }
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
                    instance.cachedViewModel = it.get()
                    instance.initialize(forceWrite = true)
                }
            }
    }

    private fun createCommonViewPathListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewPath", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance ->
                if (instance.instanceViewPath == "") {
                    instance.initialize(forceWrite = false)
                }
            }
        }
    }

    private fun createCommonViewParamsListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewParams", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance -> instance.onViewInputChange(it) }
        }
    }

    private fun createInstancesListener(): Subscription {
        return props.tree.subscribe("instances", Origin.allBut(Origin.Delegate)) { event ->
            if (event.source === this) {
                return@subscribe
            }

            val result = instanceMatch.find(event.path.toString())
            if (result !== null) {
                val index = result.groupValues[1].toInt()
                val instance = props.instances[index]
                if (instance == null) {
                    log.warnf("Error dispatching event to instance, instance %s missing.", index)
                    return@subscribe
                }

                val changeRoot =
                    event.path.toString().replace(instance.treePath.toString() + ".", "")
                if (changeRoot.startsWith("viewParams")) {
                    instance.onViewInputChange(event)
                }
            }

            val instances = toJsonDeep(event.readValue()).asJsonArray ?: JsonArray()
            val newInstanceCount = instances.size()
            if (newInstanceCount != instanceCount) {
                log.tracef("Number of instances changed, initializing views.")
                instanceCount = newInstanceCount
                props.instances.forEach { it.initialize(forceWrite = true) }
            }
        }
    }

    private fun onViewOutputChanged(viewModel: ViewModel, event: PropertyTreeChangeEvent) {
        if (event.source === this) {
            log.tracef("onViewOutputChanged - self change detected. Skipping.")
            return
        }

        val instance = props.instances.find(viewModel.id.mountPath)
        if (instance == null) {
            log.warnf(
                "Received view output params event for unknown instance %s",
                viewModel.id.mountPath
            )
            return
        }

        val newValue =
            toJsonDeep(event.readValue().value, BindingUtils.JsonEncoding.DollarQualified)

        val path = "${instance.treePath}.viewParams.${event.listeningPath}"
        val maybeCurrentValue = instance.viewParams.get(event.listeningPath.toString())
        val currentValue = toJsonDeep(maybeCurrentValue, BindingUtils.JsonEncoding.DollarQualified)

        if (currentValue == newValue) {
            log.tracef("onViewOutputChanged - %s == %s, skipping", currentValue, newValue)
            return
        }

        log.tracef("onViewOutputChanged - Writing %s to %s", newValue, path)
        instance.tree.write(path, newValue, Origin.BindingWriteback, this)
    }

    inner class InstancePropsHandler(val tree: PropertyTree, val index: Int) {

        val treePath: JsonPath = JsonPath.parse("instances[$index]")
        var cachedViewModel: ViewModel? = null

        private val commonViewPath: String
            get() {
                return toJsonDeep(
                        tree.read("instanceCommon.viewPath").orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        var instanceViewPath: String
            get() {
                return toJsonDeep(
                        tree
                            .read(treePath.createChildPath("viewPath"))
                            .orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }
            set(value) {
                tree.write(
                    treePath.createChildPath("viewPath"),
                    BasicQualifiedValue(value),
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate
                )
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

        var instanceViewParams: JsonObject
            get() {
                return toJsonDeep(
                        tree
                            .read(treePath.createChildPath("viewParams"))
                            .orElse(BasicQualifiedValue(JsonObject())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonObject ?: JsonObject()
            }
            set(value) {
                tree.write(
                    treePath.createChildPath("viewParams"),
                    BasicQualifiedValue(value),
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate
                )
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
                    addProperty("index", index)
                }
            }

        var key: String
            get() {
                return toJsonDeep(tree.read("$treePath.key").orElse(BasicQualifiedValue(index)))
                    ?.asString ?: ""
            }
            set(value) {
                tree.write("$treePath.key", BasicQualifiedValue(value), Origin.Delegate, this)
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}$${component.componentAddressPath}.$key"
            }

        fun initialize(forceWrite: Boolean) {
            onView { viewModel ->
                if (viewOutputListeners[viewModel] == null || forceWrite) {
                    syncParams(viewModel)
                }

                viewOutputListeners[viewModel]?.apply { shutdownViewOutputListeners(viewModel) }
                viewOutputListeners[viewModel] = subscribeToViewOutputs(viewModel)
            }
        }

        fun onView(block: (ViewModel) -> Unit) {
            if (cachedViewModel !== null) {
                block(cachedViewModel as ViewModel)
            } else {
                viewLoader
                    .findOrStartView(viewPath, mountPath, Date().time, viewParams)
                    .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
                    .thenAccept { maybeViewModel ->
                        if (maybeViewModel.isEmpty) {
                            component.mdc {
                                log.warnf("Failed to find view to operate on: $viewPath")
                            }
                        } else {
                            cachedViewModel = maybeViewModel.get()
                            block(maybeViewModel.get())
                        }
                    }
            }
        }

        fun onViewInputChange(event: PropertyTreeChangeEvent) {
            if (event.source === this@FlexRepeaterModelDelegate) {
                return
            }

            onView { viewModel ->
                val path = event.path.toString().replace("${treePath}.viewParams.", "")
                val value =
                    toJsonDeep(event.readCausalValue(), BindingUtils.JsonEncoding.DollarQualified)
                viewModel.writeToParams(
                    path,
                    value,
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate,
                    queue
                )
            }
        }

        private fun subscribeToViewOutputs(viewModel: ViewModel): Map<String, Subscription>? {
            return viewModel.subscribeToParams(Origin.allBut(Origin.Delegate)) {
                onViewOutputChanged(viewModel, it)
            }
        }

        fun syncParams(viewModel: ViewModel) {
            log.info("syncParams")
            val writes = JsonObject()

            val newParams = viewParams
            val currentParams = viewModel.readParams()
            newParams.keySet().forEach {
                val newValue = newParams.get(it)
                val currentValue = currentParams?.get(it)

                if (newValue != currentValue) {
                    writes.add(it, newValue)
                }
            }

            if (!writes.isEmpty) {
                log.info("${this.mountPath}, syncing params $writes")
                viewModel.writeToParams(
                    writes,
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate,
                    queue
                )
            }
        }
    }

    inner class InstancesPropsHandlers(private val tree: PropertyTree) :
        Iterable<InstancePropsHandler> {

        private val treePath: JsonPath = JsonPath.parse("instances")

        var json: JsonArray
            get() {
                return toJsonDeep(
                        tree.read(treePath).orElse(BasicQualifiedValue(JsonArray())),
                        BindingUtils.JsonEncoding.DollarQualified
                    )
                    ?.asJsonArray ?: JsonArray()
            }
            set(value) {
                tree.write(
                    treePath,
                    BasicQualifiedValue(value),
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate
                )
            }

        private val size: Int
            get() = json.size()

        operator fun get(index: Int): InstancePropsHandler? {
            if (index in 0..size) {
                return InstancePropsHandler(tree, index)
            }
            return null
        }

        fun find(mountPath: String): InstancePropsHandler? {
            return props.instances.firstOrNull { it.mountPath == mountPath }
        }

        override operator fun iterator() = iterator {
            for (index in 0..size) {
                this.yield(InstancePropsHandler(tree, index))
            }
        }

        //        private fun updateInstances(newInstances: JsonArray): JsonArray {
        //            var updateCount = 0
        //
        //            newInstances.forEachIndexed { index, instance ->
        //                val viewParams =
        //                    instance.asJsonObject?.getAsJsonObject("viewParams") ?: JsonObject()
        //
        //                updateCount += updateKey(instance.asJsonObject)
        //                updateCount += updateIndex(index, viewParams)
        //
        //                instance.asJsonObject.add("viewParams", viewParams)
        //            }
        //
        //            return newInstances
        //        }
        //
        //        private fun updateKey(instance: JsonObject): Int {
        //            val currentKey = instance.get("key")
        //            if (currentKey == null || currentKey.toString() == "") {
        //                instance.addProperty("key", UUID.randomUUID().toString())
        //                return 1
        //            }
        //            return 0
        //        }
        //
        //        private fun updateIndex(index: Int, viewParams: JsonObject): Int {
        //            val currentIndex = viewParams.get("index")
        //            if (currentIndex == null || currentIndex.toString() != index.toString()) {
        //                viewParams.addProperty("index", index)
        //                return 1
        //            }
        //            return 0
        //        }
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

    inner class MethodOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = props.instances.json
                        newInstances.remove(it[0] as Int)
                        props.instances.json = newInstances
                        null
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val newInstances = props.instances.json
                    newInstances.remove(newInstances.size() - 1)
                    props.instances.json = newInstances
                    null
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
                        props.instances.json = newInstances
                        null
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

                        props.instances.json = newInstances
                        null
                    },
                    "index" to Int::class,
                    "instance" to PyObject::class
                )
                .build()
    }
}
