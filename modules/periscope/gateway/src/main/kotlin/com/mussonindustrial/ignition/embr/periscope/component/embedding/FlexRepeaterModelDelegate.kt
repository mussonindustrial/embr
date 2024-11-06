package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.JsonPath
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.common.util.get
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.api.ViewInstanceId
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.inductiveautomation.perspective.gateway.property.PropertyTreeChangeEvent
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.reflect.ViewLoader
import com.mussonindustrial.embr.perspective.gateway.reflect.getHandlers
import com.mussonindustrial.ignition.embr.periscope.page.ViewJoinMsg
import java.util.*
import org.python.core.PyObject

class FlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log: LoggerEx =
        LogUtil.getModuleLogger("embr-periscope", "FlexRepeaterModelDelegate")
    private val id = "${component.view?.id?.resourcePath}${component.componentAddressPath}"

    private val queue = component.session.queue()
    private val pyArgsOverloads = PyArgOverloads()
    private val viewLoader = ViewLoader(component.page as PageModel)
    private val viewOutputListeners: WeakHashMap<ViewModel, List<Subscription>?> = WeakHashMap()
    private val props = component.getPropertyTreeOf(PropertyType.props)!!
    private val instancesListener = createInstanceListener()

    private var instances: JsonArray
        get() {
            val instancesProp = props.read("instances")
            if (instancesProp.isEmpty) {
                return JsonArray()
            }

            return toJsonDeep(instancesProp.get()).asJsonArray
        }
        set(value) {
            props.write("instances", value, Origin.Delegate, this)
        }

    private val instanceCommon: JsonObject
        get() {
            val instanceCommon = props.read("instanceCommon")
            if (instanceCommon.isEmpty) {
                return JsonObject()
            }

            return toJsonDeep(instanceCommon.get()).asJsonObject
        }

    override fun onStartup() {
        log.debug("$id: Startup")
        maybeRegisterHandlers()
        queue.submit {
            updateInstances(instances, forceWrite = false)
            updateChildViews()
        }
    }

    override fun onShutdown() {
        log.debug("$id: Shutdown")
        instancesListener.unsubscribe()
        shutdownChildViewListeners()
    }

    private fun maybeRegisterHandlers() {
        val pageModel = component.page as PageModel
        val nativeHandlers = pageModel.getHandlers()
        if (!nativeHandlers.handles(ViewJoinMsg.PROTOCOL)) {
            nativeHandlers.register(
                ViewJoinMsg.PROTOCOL,
                ViewJoinMsg.joinOrStart(pageModel),
                ViewJoinMsg::class.java
            )
        }
    }

    private fun createInstanceListener(): Subscription {
        return props.subscribe("instances", Origin.allBut(Origin.Delegate)) {
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
                log.trace("${component.componentAddressPath} - performing instance repair")
                log.trace("Instances: $newInstances")
            }

            instances = newInstances
        }
    }

    private fun updateKey(instance: JsonObject): Int {
        val currentKey = instance.get("key")
        if (currentKey == null || currentKey.toString() == "") {
            log.trace("$id: updating key for $instance")
            instance.addProperty("key", UUID.randomUUID().toString())
            return 1
        }
        return 0
    }

    private fun updateIndex(index: Int, viewParams: JsonObject): Int {
        val currentIndex = viewParams.get("index")
        if (currentIndex == null || currentIndex.toString() != index.toString()) {
            log.trace("$id: updating index for $viewParams")
            viewParams.addProperty("index", index)
            return 1
        }
        return 0
    }

    private fun getChildMountPath(instance: JsonObject): String {
        return "${component.view?.id?.mountPath}$${component.componentAddressPath}.${instance.get("key").asString}"
    }

    private fun updateChildViews() =
        onInstanceViews(instances) { view ->
            val tree =
                view.viewModel?.getPropertyTreeOf(PropertyType.params) ?: return@onInstanceViews

            updateChildViewParams(tree, view)
            if (viewOutputListeners[view.viewModel] == null) {
                createChildViewListeners(tree, view)
            }
        }

    private fun updateChildViewParams(tree: PropertyTree, view: ViewInstance) {
        val childParams = toJsonDeep(tree.read(JsonPath.ROOT).get().value) as JsonObject
        val viewParams = getChildViewParams(view.configuration.asJsonObject)

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

    private fun getChildViewParams(instance: JsonObject): JsonObject {
        val instanceCommonParams = instanceCommon.get("viewParams")?.asJsonObject
        val childParams = instance.get("viewParams")?.asJsonObject

        return JsonObject().apply {
            instanceCommonParams?.entrySet()?.forEach { add(it.key, it.value) }
            childParams?.entrySet()?.forEach { add(it.key, it.value) }
        }
    }

    private fun createChildViewListeners(tree: PropertyTree, view: ViewInstance) {
        log.trace("$id: createChildViewListeners - creating listeners")
        val listeners =
            tree.rootKeys?.map { rootKey ->
                tree.subscribe(rootKey, Origin.allBut(Origin.Delegate)) { event ->
                    if (this.isRunning && view.viewModel?.isRunning == true) {
                        this.onViewOutputChanged(view.viewModel, event)
                    } else {
                        log.warn("$id: orphaned child view listener fired, cleaning up.")
                        viewOutputListeners[view.viewModel]?.forEach { it.unsubscribe() }
                        viewOutputListeners[view.viewModel] = null
                    }
                }
            }
        viewOutputListeners[view.viewModel] = listeners
    }

    private fun shutdownChildViewListeners() {
        log.debug("$id: Removing child view listeners")
        viewOutputListeners.forEach { listenerSet ->
            listenerSet.value?.forEach { it.unsubscribe() }
        }
        viewOutputListeners.clear()
    }

    private fun onViewOutputChanged(viewModel: ViewModel, event: PropertyTreeChangeEvent) {

        val instanceIndex =
            instances.indexOfFirst { getChildMountPath(it.asJsonObject) == viewModel.id.mountPath }

        if (instanceIndex == -1) {
            log.warn("$id: Received a view-output-changed event for an unknown instance.")
            return
        }

        val path = "instances[${instanceIndex}].viewParams.${event.listeningPath}"

        var currentValue: Any? = null
        val maybeCurrentValue = props.read(path)
        if (maybeCurrentValue.isPresent) {
            currentValue = maybeCurrentValue.get().value
        }

        if (currentValue == event.readValue().value) {
            return
        }

        props.write(
            "instances[${instanceIndex}].viewParams.${event.listeningPath}",
            event.readValue(),
            Origin.BindingWriteback,
            this
        )
    }

    data class ViewInstance(
        val index: Int,
        val configuration: JsonElement,
        val viewModel: ViewModel?
    )

    private fun onInstanceViews(instances: JsonArray, block: (ViewInstance) -> Unit) {
        instances.forEachIndexed { index, instance ->
            queue.submitOrRun {
                val viewPath = getChildViewPath(instance.asJsonObject)
                val mountPath = getChildMountPath(instance.asJsonObject)
                val viewInstanceId = ViewInstanceId(viewPath, mountPath)

                val futureView = viewLoader.findView(viewInstanceId)
                futureView.thenAcceptAsync(
                    { maybeViewModel ->
                        maybeViewModel.ifPresentOrElse(
                            {
                                val viewModel = maybeViewModel.get()
                                block(ViewInstance(index, instance, viewModel))
                            },
                            {
                                viewLoader.startView(
                                    viewPath,
                                    mountPath,
                                    Date().time,
                                    getChildViewParams(instance.asJsonObject)
                                )

                                viewLoader
                                    .waitForView(viewInstanceId, queue, 10000)
                                    .thenAcceptAsync(
                                        {
                                            if (it.isEmpty) {
                                                return@thenAcceptAsync
                                            }
                                            block(ViewInstance(index, instance, it.get()))
                                        },
                                        queue::submit
                                    )
                            }
                        )
                    },
                    queue::submit
                )
            }
        }
    }

    private fun getChildViewPath(instance: JsonObject): String {
        val viewPath = instance.get("viewPath")
        if (viewPath != null) {
            return viewPath.asString
        }

        val maybeCommonPath = props.read("instanceCommon.viewPath")
        if (maybeCommonPath.isPresent) {
            return maybeCommonPath.get().value as? String ?: ""
        }

        return ""
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["index"],
        types = [Int::class],
    )
    fun popInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { pyArgsOverloads.popInstance.call(args, keywords) }

    @ScriptCallable
    @KeywordArgs(
        names = ["instance"],
        types = [PyObject::class],
    )
    fun pushInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { pyArgsOverloads.pushInstance.call(args, keywords) }

    @ScriptCallable
    @KeywordArgs(
        names = ["index", "instance"],
        types = [Int::class, PyObject::class],
    )
    fun insertInstance(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { pyArgsOverloads.insertInstance.call(args, keywords) }

    inner class PyArgOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = instances
                        newInstances.remove(it[0] as Int)
                        updateInstances(newInstances, true)
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val newInstances = instances
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
                        val newInstances = instances

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

                        val existingInstances = instances
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
