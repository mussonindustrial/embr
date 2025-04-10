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
import com.mussonindustrial.embr.perspective.gateway.model.subscribeToParams
import com.mussonindustrial.embr.perspective.gateway.model.writeToParams
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.api.ViewJoinMsg
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.collections.drop
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.take
import kotlin.reflect.typeOf
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
    private val instanceViews = HashMap<String, WeakReference<ViewModel>>()
    private val viewTimeoutMs = 10_000L

    private val instanceMatch = Regex("instances\\[(\\d+)]\\.(.*+)")

    override fun onStartup() {
        component.mdc {
            log.debugf("Startup")
            props.instances.forEach { initializeView(it) }
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
        viewOutputListeners.keys.forEach { shutdownViewOutputListeners(it) }
    }

    private fun shutdownViewOutputListeners(viewModel: ViewModel) {
        viewOutputListeners[viewModel]?.apply {
            component.mdc {
                log.tracef("Removing view output listeners for view %s", viewModel.id.mountPath)
                forEach { it.value.unsubscribe() }
                viewOutputListeners[viewModel] = null
            }
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        try {
            component.mdcSetup()
            log.tracef("Received message for protocol [%s].", message.eventName)

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

        log.tracef("Client is requesting to join view %s", event.mountPath)

        viewLoader
            .findOrStartView(
                event.resourcePath,
                event.mountPath,
                event.birthDate,
                instance.viewParams,
            )
            .orTimeout(viewTimeoutMs, TimeUnit.MILLISECONDS)
            .thenAcceptAsync(
                {
                    if (it.isPresent) {
                        initializeView(instance, it.get())
                    }
                },
                queue::submit,
            )
    }

    private fun createCommonViewPathListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewPath", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance ->
                if (instance.instanceViewPath == "") {
                    initializeView(instance)
                }
            }
        }
    }

    private fun createCommonViewParamsListener(): Subscription {
        return props.tree.subscribe("instanceCommon.viewParams", Origin.allBut(Origin.Delegate)) {
            props.instances.forEach { instance -> onViewCommonChanged(instance, it) }
        }
    }

    private fun createInstancesListener(): Subscription {
        return props.tree.subscribe("instances", Origin.allBut(Origin.Delegate)) { event ->
            if (event.source == this) {
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
                    log.trace(
                        "Dispatching change event on ${event.path} to instance ${instance.mountPath}"
                    )
                    this.onViewInputChanged(instance, event)
                }
            }

            props.instances.forEach {
                if (it.cachedViewModel == null) {
                    component.mdc {
                        log.trace("New view instance found, initializing instance ${it.mountPath}")
                    }
                    initializeView(it)
                }
            }
        }
    }

    private fun createViewOutputListeners(viewModel: ViewModel): Map<String, Subscription>? {
        component.mdc { log.tracef("Subscribing to view outputs: ${viewModel.id.mountPath}") }
        return viewModel.subscribeToParams(Origin.allBut(Origin.Delegate)) {
            viewModel.mdc { log.tracef("Outputs changed, firing: ${it.path}") }
            this.onViewOutputChanged(viewModel, it)
        }
    }

    private fun initializeView(instance: InstancePropsHandler) =
        instance.onView { viewModel -> initializeView(instance, viewModel) }

    private fun initializeView(instance: InstancePropsHandler, viewModel: ViewModel) {
        viewModel.writeToParams(instance.viewParams, Origin.Delegate, this)

        viewOutputListeners[viewModel]?.apply { shutdownViewOutputListeners(viewModel) }
        viewOutputListeners[viewModel] = createViewOutputListeners(viewModel)
    }

    private fun onViewCommonChanged(
        instance: InstancePropsHandler,
        event: PropertyTreeChangeEvent,
    ) {
        if (event.source == this) {
            return
        }

        val rootKey = event.path.pathElements[1]?.toString()
        if (instance.instanceViewParams.has(rootKey)) {
            return
        }

        instance.onView { viewModel ->
            val path = event.path.toString().replace("instanceCommon.viewParams.", "")
            val value =
                toJsonDeep(event.readCausalValue(), BindingUtils.JsonEncoding.DollarQualified)
            viewModel.writeToParams(path, value, Origin.Delegate, this)
        }
    }

    private fun onViewInputChanged(instance: InstancePropsHandler, event: PropertyTreeChangeEvent) {
        if (event.source == this) {
            return
        }

        instance.onView { viewModel ->
            val path = event.path.toString().replace("${instance.treePath}.viewParams.", "")
            val value =
                toJsonDeep(event.readCausalValue(), BindingUtils.JsonEncoding.DollarQualified)
            viewModel.writeToParams(path, value, Origin.Delegate, this)
        }
    }

    private fun onViewOutputChanged(viewModel: ViewModel, event: PropertyTreeChangeEvent) {
        if (event.source == this) {
            return
        }

        val instance = props.instances.find(viewModel.id.mountPath)
        if (instance == null) {
            log.warnf(
                "Received view output params event for unknown instance %s",
                viewModel.id.mountPath,
            )
            return
        }

        val newValue =
            toJsonDeep(event.readValue().value, BindingUtils.JsonEncoding.DollarQualified)

        val path = "${instance.treePath}.viewParams.${event.listeningPath}"
        val currentValue =
            toJsonDeep(
                instance.viewParams.get(event.listeningPath.toString()),
                BindingUtils.JsonEncoding.DollarQualified,
            )

        if (currentValue == newValue) {
            return
        }

        props.tree.write(path, newValue, Origin.BindingWriteback, this)
    }

    inner class InstancePropsHandler(private val tree: PropertyTree, private val index: Int) {

        val treePath: JsonPath = JsonPath.parse("instances[$index]")
        var cachedViewModel: ViewModel?
            get() {
                return instanceViews[key]?.get()
            }
            set(value) {
                instanceViews[key] = WeakReference(value)
            }

        private val commonViewPath: String
            get() {
                return toJsonDeep(
                        tree.read("instanceCommon.viewPath").orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        val instanceViewPath: String
            get() {
                return toJsonDeep(
                        tree
                            .read(treePath.createChildPath("viewPath"))
                            .orElse(BasicQualifiedValue(""))
                    )
                    ?.asString ?: ""
            }

        private val commonViewParams: JsonObject
            get() {
                return toJsonDeep(
                        tree
                            .read("instanceCommon.viewParams")
                            .orElse(BasicQualifiedValue(JsonObject())),
                        BindingUtils.JsonEncoding.DollarQualified,
                    )
                    ?.asJsonObject ?: JsonObject()
            }

        val instanceViewParams: JsonObject
            get() {
                return toJsonDeep(
                        tree
                            .read(treePath.createChildPath("viewParams"))
                            .orElse(BasicQualifiedValue(JsonObject())),
                        BindingUtils.JsonEncoding.DollarQualified,
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
                    addProperty("key", key)
                    addProperty("index", index)
                    commonViewParams.entrySet()?.forEach { add(it.key, it.value) }
                    instanceViewParams.entrySet()?.forEach { add(it.key, it.value) }
                }
            }

        private val key: String
            get() {
                val hasKey = tree.hasProperty("$treePath.key")
                if (hasKey) {
                    return toJsonDeep(tree.read("$treePath.key").get()).asString
                } else {
                    val newKey = UUID.randomUUID().toString()
                    tree.write("$treePath.key", BasicQualifiedValue(newKey), Origin.Delegate, this)
                    return newKey
                }
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}$${component.componentAddressPath}.$key"
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
    }

    inner class InstancesPropsHandlers(private val tree: PropertyTree) :
        Iterable<InstancePropsHandler> {

        private val treePath: JsonPath = JsonPath.parse("instances")

        var json: JsonArray
            get() {
                return toJsonDeep(
                        tree.read(treePath).orElse(BasicQualifiedValue(JsonArray())),
                        BindingUtils.JsonEncoding.DollarQualified,
                    )
                    ?.asJsonArray ?: JsonArray()
            }
            set(value) {
                val modifiedInstances = mutableListOf<Int>()

                value.forEachIndexed { index, instance ->
                    var updateCount = 0
                    val viewParams =
                        instance.asJsonObject?.getAsJsonObject("viewParams") ?: JsonObject()

                    updateCount += updateKey(instance.asJsonObject)
                    updateCount += updateIndex(index, viewParams)

                    instance.asJsonObject.add("viewParams", viewParams)

                    if (updateCount > 0) {
                        modifiedInstances.add(index)
                    }
                }

                tree.write(
                    treePath,
                    BasicQualifiedValue(value),
                    Origin.Delegate,
                    this@FlexRepeaterModelDelegate,
                )

                modifiedInstances.forEach {
                    val instance = this[it]
                    if (instance != null) {
                        initializeView(instance)
                    }
                }
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
    }

    inner class PropsHandler(val tree: PropertyTree) {
        val instances = InstancesPropsHandlers(tree)
    }

    @ScriptCallable
    @KeywordArgs(names = ["index"], types = [Int::class])
    @Suppress("unused")
    fun popInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.popInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(names = ["instance"], types = [PyObject::class])
    @Suppress("unused")
    fun pushInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.pushInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(names = ["index", "instance"], types = [Int::class, PyObject::class])
    @Suppress("unused")
    fun insertInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.insertInstance.call(args, keywords) } }

    inner class MethodOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = props.instances.json
                        newInstances.remove(it["index"] as Int)
                        props.instances.json = newInstances
                        null
                    },
                    "index" to typeOf<Int>(),
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
                        val instance = TypeUtilities.pyToGson(it["instance"] as PyObject?)
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
                    "instance" to typeOf<PyObject>(),
                )
                .build()

        val insertInstance =
            PyArgOverloadBuilder()
                .setName("insertInstance")
                .addOverload(
                    {
                        val index = it["index"] as Int
                        val instance = TypeUtilities.pyToGson(it["instance"] as PyObject?)

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
                    "index" to typeOf<Int>(),
                    "instance" to typeOf<PyObject>(),
                )
                .build()
    }
}
