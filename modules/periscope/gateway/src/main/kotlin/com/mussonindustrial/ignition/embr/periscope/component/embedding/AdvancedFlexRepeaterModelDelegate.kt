package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.common.reflect.getSuperPrivateMethod
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import java.lang.reflect.Method
import java.util.*
import org.python.core.PyObject

class AdvancedFlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val logger = this.getLogger()
    private val pyArgsOverloads = PyArgOverloads()
    private lateinit var props: PropertyTree
    private lateinit var instancesListener: Subscription

    private var UNSTABLE_pageModelHandler: Any? = null
    private var UNSTABLE_startView: Method? = null

    override fun onStartup() {
        logger.debug("Model Delegate starting for ${component.componentAddressPath}")
        configurePreemptiveLoad()

        props = component.getPropertyTreeOf(PropertyType.props)!!

        repairInstances()
        instancesListener = props.subscribe("instances", Origin.ANY) { repairInstances() }
    }

    override fun onShutdown() {
        logger.debug("Model Delegate stopping for ${component.componentAddressPath}")
        instancesListener.unsubscribe()
    }

    private fun configurePreemptiveLoad() {
        try {
            val pageModel = component.page as? PageModel
            UNSTABLE_pageModelHandler = pageModel?.getSuperPrivateProperty("handlers")
            if (UNSTABLE_pageModelHandler == null) {
                logger.warn(
                    "`pageModel.handlers` not found. Pre-emptive loading will not function."
                )
                return
            }

            UNSTABLE_startView =
                UNSTABLE_pageModelHandler?.getSuperPrivateMethod(
                    "startView",
                    String::class.java,
                    String::class.java,
                    Long::class.java,
                    JsonObject::class.java
                )
            if (UNSTABLE_startView == null) {
                logger.warn(
                    "`pageModel.handlers.startView` method not found. Pre-emptive loading will not function."
                )
                return
            }
        } catch (e: ReflectiveOperationException) {
            logger.warn(
                "Exception occurred enabling pre-emptive loading. Pre-emptive loading will not function.",
                e
            )
        }
    }

    private fun updateKey(index: Int, instance: JsonObject): Int {
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

    private fun repairInstances() {
        val instances = getInstances()
        repairInstances(instances)
    }

    private fun repairInstances(instances: JsonArray?) {
        repairInstances(instances, false)
    }

    private fun repairInstances(instances: JsonArray?, forceWrite: Boolean?) {
        var updateCount = 0

        instances?.forEachIndexed { index, instance ->
            val viewParams = instance.asJsonObject?.getAsJsonObject("viewParams") ?: JsonObject()

            updateCount += updateKey(index, instance.asJsonObject)
            updateCount += updateIndex(index, viewParams)

            instance.asJsonObject.add("viewParams", viewParams)
        }

        if (updateCount > 0 || forceWrite == true) {
            logger.trace("${component.componentAddressPath} - performing instance repair")
            props.queue.submit {
                props.write("instances", instances, Origin.Delegate, this)
                doPreemptiveChildUpdate()
            }
        }
    }

    private fun getInstances(): JsonArray? {
        val instancesProp = props.read("instances")
        if (instancesProp.isEmpty) {
            return null
        }

        return toJsonDeep(instancesProp.get()).asJsonArray
    }

    private fun getInstanceCommon(): JsonObject? {
        val instanceCommon = props.read("instanceCommon")
        if (instanceCommon.isEmpty) {
            return null
        }

        return toJsonDeep(instanceCommon.get()).asJsonObject
    }

    private fun getChildViews(): Map<JsonElement, ViewModel?>? {
        val page = component.page
        val instances = getInstances()

        return instances?.associateWith {
            val key = it.asJsonObject.get("key").asString
            val view =
                page?.views?.find { view ->
                    view.qualifiedPath.contains("${component.componentAddressPath}.$key")
                }
            view
        }
    }

    private fun getChildViewPath(instance: JsonObject): String {
        return instance.get("viewPath")?.asString
            ?: props.read("instanceCommon.viewPath").get().value as String
    }

    private fun getChildMountPath(instance: JsonObject): String {
        return "${component.view?.id?.mountPath}$${component.componentAddressPath}.${instance.get("key").asString}"
    }

    private fun getChildViewParams(instance: JsonObject): JsonObject {
        val instanceCommonParams = getInstanceCommon()?.get("viewParams")?.asJsonObject
        val childParams = instance.get("viewParams")?.asJsonObject

        return JsonObject().apply {
            instanceCommonParams?.entrySet()?.forEach { add(it.key, it.value) }
            childParams?.entrySet()?.forEach { add(it.key, it.value) }
        }
    }

    private fun doPreemptiveChildUpdate() {
        getChildViews()?.forEach { (instance, view) ->
            if (view != null) {
                updateChildViewParams(instance.asJsonObject, view)
            } else {
                UNSTABLE_startViewForInstance(instance.asJsonObject)
            }
        }
    }

    private fun updateChildViewParams(instance: JsonObject, view: ViewModel) {
        val childParams = view.getPropertyTreeOf(PropertyType.params)
        val viewParams = getChildViewParams(instance.asJsonObject)

        val writes = JsonObject()
        viewParams.keySet().forEach { writes.add(it, viewParams.get(it)) }
        if (!writes.isEmpty) {
            childParams?.queue?.submit { childParams.writeAll(writes, Origin.Delegate, this) }
        }
    }

    private fun UNSTABLE_startView(
        viewPath: String,
        mountPath: String,
        birthDate: Long,
        params: JsonObject
    ) {
        if (UNSTABLE_startView == null) {
            return
        }
        UNSTABLE_startView?.invoke(
            UNSTABLE_pageModelHandler,
            viewPath,
            mountPath,
            birthDate,
            params
        )
    }

    private fun UNSTABLE_startViewForInstance(instance: JsonObject) {
        val viewPath = getChildViewPath(instance)
        val mountPath = getChildMountPath(instance)
        val params = getChildViewParams(instance)
        val birthDate = Date().time
        UNSTABLE_startView(viewPath, mountPath, birthDate, params)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["index"],
        types = [Int::class],
    )
    fun popInstance(args: Array<PyObject>, keywords: Array<String>) =
        props.queue.submit { pyArgsOverloads.popInstance.call(args, keywords) }

    @ScriptCallable
    @KeywordArgs(
        names = ["instance"],
        types = [PyObject::class],
    )
    fun pushInstance(args: Array<PyObject>, keywords: Array<String>) =
        props.queue.submit { pyArgsOverloads.pushInstance.call(args, keywords) }

    @ScriptCallable
    @KeywordArgs(
        names = ["index", "instance"],
        types = [Int::class, PyObject::class],
    )
    fun insertInstance(args: Array<PyObject>, keywords: Array<String>) =
        props.queue.submit { pyArgsOverloads.insertInstance.call(args, keywords) }

    inner class PyArgOverloads {
        val popInstance =
            PyArgOverloadBuilder()
                .setName("popInstance")
                .addOverload(
                    {
                        val newInstances = getInstances()
                        newInstances?.remove(it[0] as Int)
                        repairInstances(newInstances, true)
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val newInstances = getInstances()
                    newInstances?.remove(newInstances.size() - 1)
                    repairInstances(newInstances, true)
                })
                .build()

        val pushInstance =
            PyArgOverloadBuilder()
                .setName("pushInstance")
                .addOverload(
                    {
                        val instance = TypeUtilities.pyToGson(it[0] as PyObject?)
                        val newInstances = getInstances()

                        if (instance.isJsonObject) {
                            newInstances?.add(instance)
                        } else if (instance.isJsonArray) {
                            newInstances?.addAll(instance.asJsonArray)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        repairInstances(newInstances, true)
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

                        val existingInstances = getInstances()
                        val originalSize = existingInstances?.size()
                        val newInstances =
                            JsonArray().apply {
                                existingInstances?.take(index)?.forEach { instance ->
                                    add(instance)
                                }
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
                            if (originalSize != null) {
                                existingInstances.drop(index).forEach { instance -> add(instance) }
                            }
                        }

                        repairInstances(newInstances, true)
                    },
                    "index" to Int::class,
                    "instance" to PyObject::class
                )
                .build()
    }
}
