package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.JsonPath
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.*
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader
import com.mussonindustrial.ignition.embr.periscope.page.ViewJoinMsg
import java.util.*
import org.python.core.PyObject
import org.slf4j.MDC

class FlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-periscope", "FlexRepeaterModelDelegate")
    private val queue = component.session.queue()
    private val props = PropsHandler(component.getPropertyTreeOf(PropertyType.props)!!)
    private val pyArgsOverloads = PyArgOverloads()
    private val viewLoader = ViewLoader(component.page as PageModel)
    private val viewOutputListeners: WeakHashMap<ViewModel, List<Subscription>?> = WeakHashMap()
    private val instancesListener = createInstanceListener()

    override fun onStartup() {
        log.debug("Startup")
        queue.submit {
            updateInstances(props.instances, forceWrite = false)
            updateChildViews()
        }
    }

    override fun onShutdown() {
        log.debug("Shutdown")
        instancesListener.unsubscribe()
        shutdownChildViewListeners()
    }

    private fun shutdownChildViewListeners() {
        component.mdc {
            log.debug("Removing child view listeners.")
            viewOutputListeners.forEach { listenerSet ->
                listenerSet.value?.forEach { it.unsubscribe() }
            }
            viewOutputListeners.clear()
        }
    }

    override fun handleEvent(message: EventFiredMsg) {
        if (message.eventName == ViewJoinMsg.PROTOCOL) {
            val instanceId =
                ViewInstanceId(
                    message.event.get("resourcePath").asString,
                    message.event.get("mountPath").asString
                )

            viewLoader.findView(instanceId).thenAccept { maybeView ->
                if (maybeView.isEmpty) {
                    viewLoader.startView(
                        message.event.get("resourcePath").asString,
                        message.event.get("mountPath").asString,
                        message.event.get("birthDate").asLong,
                        message.event.get("params").asJsonObject
                    )
                }
                updateChildViews()
            }
        }
    }

    private fun createInstanceListener(): Subscription {
        return props.tree.subscribe("instances", Origin.allBut(Origin.Delegate)) {
            queue.submit {
                updateInstances(
                    it.readValue().value as? JsonArray ?: JsonArray(),
                    forceWrite = false
                )
                updateChildViews()
            }
        }
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

            props.instances = newInstances
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

    private fun updateChildViews() {
        runOnViews(props.instances) { view ->
            if (view.viewModel == null) {
                component.mdc { log.warn("Missing viewModel for ${view.mountPath}") }
                return@runOnViews
            }

            view.updateViewModel()
            if (viewOutputListeners[view.viewModel] == null) {

                if (log.isTraceEnabled) {
                    view.viewModel?.mdc { log.trace("Creating view output listeners.") }
                }

                val listeners =
                    view.createRootParamSubscriptions { event ->
                        if (this.isRunning && view.viewModel!!.isRunning) {
                            this.onViewOutputChanged(view.viewModel!!, event)
                        } else {
                            component.mdc {
                                log.warn("Orphaned child view listener fired, cleaning up.")
                                viewOutputListeners[view.viewModel]?.forEach { it.unsubscribe() }
                                viewOutputListeners[view.viewModel] = null
                            }
                        }
                    }

                viewOutputListeners[view.viewModel] = listeners
            }
        }
    }

    private fun onViewOutputChanged(viewModel: ViewModel, event: PropertyTreeChangeEvent) {
        if (log.isTraceEnabled) {
            component.mdc {
                log.trace("View output change event: ${event.listeningPath}=${event.readValue()}")
            }
        }

        val instanceIndex =
            props.instances.indexOfFirst {
                ViewInstance(0, it.asJsonObject, null).mountPath == viewModel.id.mountPath
            }

        if (instanceIndex == -1) {
            component.mdc {
                log.warn("Received a view-output-changed event for an unknown instance.")
            }
            return
        }

        val path = "instances[${instanceIndex}].viewParams.${event.listeningPath}"

        var currentValue: Any? = null
        val maybeCurrentValue = props.tree.read(path)
        if (maybeCurrentValue.isPresent) {
            currentValue = maybeCurrentValue.get().value
        }

        if (currentValue == event.readValue().value) {
            component.mdc {
                if (log.isTraceEnabled) {
                    log.trace("No value change, skipping.")
                }
            }
            return
        }

        component.mdc {
            if (log.isTraceEnabled) {
                log.trace("Writing $path=[${event.readValue()}]")
            }
        }
        props.tree.write(path, event.readValue(), Origin.BindingWriteback, this)
    }

    private fun runOnViews(instances: JsonArray, block: (ViewInstance) -> Unit) {
        instances.forEachIndexed { index, instance ->
            queue.submit {
                val view = ViewInstance(index, instance.asJsonObject, null)
                val futureView = viewLoader.findView(view.viewInstanceId)

                futureView.thenAcceptAsync({ runOrLoadView(it, view, 10000, block) }, queue::submit)
            }
        }
    }

    private fun runOrLoadView(
        maybeViewModel: Optional<ViewModel>,
        viewInstance: ViewInstance,
        waitLimitMs: Int,
        block: (ViewInstance) -> Unit
    ) {
        if (maybeViewModel.isPresent) {
            viewInstance.viewModel = maybeViewModel.get()
            queue.submitOrRun { block(viewInstance) }
            return
        } else {
            if (log.isTraceEnabled) {
                viewInstance.mdc {
                    log.trace("${viewInstance.viewInstanceId.id} isn't running, starting...")
                }
            }

            viewLoader.startView(
                viewInstance.viewPath,
                viewInstance.mountPath,
                Date().time,
                viewInstance.viewParams
            )
            viewLoader
                .waitForView(viewInstance.viewInstanceId, queue, waitLimitMs)
                .thenAcceptAsync(
                    {
                        viewInstance.mdc {
                            if (it.isEmpty) {
                                viewInstance.mdc {
                                    log.warn("Could not find or start view within wait limit.")
                                }
                                return@mdc
                            }
                            viewInstance.viewModel = it.get()

                            if (log.isTraceEnabled) {
                                viewInstance.mdc { log.trace("View started.") }
                            }
                            block(viewInstance)
                        }

                        return@thenAcceptAsync
                    },
                    queue::submit
                )
            return
        }
    }

    inner class ViewInstance(
        private val index: Int,
        private val configuration: JsonObject,
        var viewModel: ViewModel?
    ) : LoggingContext {

        private val key: String
            get() = configuration.get("key").asString

        val viewParams: JsonObject
            get() {
                val instanceCommonParams = props.instanceCommon.get("viewParams")?.asJsonObject
                val childParams = configuration.get("viewParams")?.asJsonObject

                return JsonObject().apply {
                    instanceCommonParams?.entrySet()?.forEach { add(it.key, it.value) }
                    childParams?.entrySet()?.forEach { add(it.key, it.value) }
                }
            }

        val viewPath: String
            get() {
                val viewPath = configuration.get("viewPath")
                if (viewPath != null) {
                    return viewPath.asString
                }

                val maybeCommonPath = props.tree.read("instanceCommon.viewPath")
                if (maybeCommonPath.isPresent) {
                    return maybeCommonPath.get().value as? String ?: ""
                }

                return ""
            }

        val mountPath: String
            get() {
                return "${component.view?.id?.mountPath}$${component.componentAddressPath}.${key}"
            }

        val viewInstanceId: ViewInstanceId
            get() = ViewInstanceId(viewPath, mountPath)

        fun updateViewModel() {
            val tree = viewModel?.getPropertyTreeOf(PropertyType.params) ?: return
            val childParams = toJsonDeep(tree.read(JsonPath.ROOT).get().value) as JsonObject

            val writes = JsonObject()
            viewParams.keySet().forEach {
                if (childParams.get(it) != viewParams.get(it)) {
                    writes.add(it, viewParams.get(it))
                }
            }
            if (!writes.isEmpty) {
                queue.submit { tree.writeAll(writes, Origin.Delegate, this) }
            }
        }

        fun createRootParamSubscriptions(
            block: (PropertyTreeChangeEvent) -> Unit
        ): List<Subscription>? {
            mdc {}
            val tree = viewModel?.getPropertyTreeOf(PropertyType.params) ?: return null

            if (log.isTraceEnabled) {
                mdc { log.trace("Creating subscriptions for ${tree.rootKeys}") }
            }

            val listeners =
                tree.rootKeys?.map { rootKey ->
                    tree.subscribe(rootKey, Origin.allBut(Origin.Delegate), block)
                }

            return listeners
        }

        override fun getLogger(): LoggerEx = log

        override fun getMdcParent(): Component = component

        override fun mdcSetup() {
            MDC.put("instances.index", index.toString())
            MDC.put("instance.view", viewInstanceId.id)
        }

        override fun mdcTeardown() {
            MDC.remove("instances.index")
            MDC.remove("instance.view")
        }
    }

    class PropsHandler(val tree: PropertyTree) {
        var instances: JsonArray
            get() {
                val instancesProp = tree.read("instances")
                if (instancesProp.isEmpty) {
                    return JsonArray()
                }

                return toJsonDeep(instancesProp.get()).asJsonArray
            }
            set(value) {
                tree.write("instances", value, Origin.Delegate, this)
            }

        val instanceCommon: JsonObject
            get() {
                val instanceCommon = tree.read("instanceCommon")
                if (instanceCommon.isEmpty) {
                    return JsonObject()
                }

                return toJsonDeep(instanceCommon.get()).asJsonObject
            }
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["index"],
        types = [Int::class],
    )
    fun popInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { pyArgsOverloads.popInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["instance"],
        types = [PyObject::class],
    )
    fun pushInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { pyArgsOverloads.pushInstance.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["index", "instance"],
        types = [Int::class, PyObject::class],
    )
    fun insertInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { pyArgsOverloads.insertInstance.call(args, keywords) } }

    inner class PyArgOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = props.instances
                        newInstances.remove(it[0] as Int)
                        updateInstances(newInstances, true)
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val newInstances = props.instances
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
                        val newInstances = props.instances

                        if (instance.isJsonObject) {
                            newInstances.add(instance)
                        } else if (instance.isJsonArray) {
                            newInstances.addAll(instance.asJsonArray)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        queue.submit {
                            updateInstances(newInstances, true)
                            updateChildViews()
                        }
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

                        val existingInstances = props.instances
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

                        queue.submit {
                            updateInstances(newInstances, true)
                            updateChildViews()
                        }
                    },
                    "index" to Int::class,
                    "instance" to PyObject::class
                )
                .build()
    }
}
