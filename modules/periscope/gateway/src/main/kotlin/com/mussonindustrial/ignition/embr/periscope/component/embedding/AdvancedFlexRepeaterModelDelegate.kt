package com.mussonindustrial.ignition.embr.periscope.component.embedding

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonArray
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.perspective.common.api.PropertyType
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.property.PropertyTree.Subscription
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import java.util.*
import org.python.core.PyObject

class AdvancedFlexRepeaterModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private lateinit var props: PropertyTree
    private val logger = this.getLogger()
    private lateinit var instancesListener: Subscription
    private val pyArgsOverloads = PyArgOverloads()

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
                        val existingInstances = getInstances()
                        existingInstances?.remove(it[0] as Int)
                        fixAndWrite(existingInstances, true)
                    },
                    "index" to Int::class,
                )
                .addOverload({
                    val existingInstances = getInstances()
                    existingInstances?.remove(existingInstances.size() - 1)
                    fixAndWrite(existingInstances, true)
                })
                .build()

        val pushInstance =
            PyArgOverloadBuilder()
                .setName("pushInstance")
                .addOverload(
                    {
                        val instance = TypeUtilities.pyToGson(it[0] as PyObject?)
                        val existingInstances = getInstances()

                        if (instance.isJsonObject) {
                            existingInstances?.add(instance)
                        } else if (instance.isJsonArray) {
                            existingInstances?.addAll(instance as JsonArray?)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        fixAndWrite(existingInstances, true)
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
                            newInstances.addAll(instance as JsonArray?)
                        } else {
                            throw IllegalArgumentException(
                                "instance must be an object or a list of objects"
                            )
                        }

                        newInstances.apply {
                            if (originalSize != null) {
                                existingInstances.drop(originalSize - index).forEach { instance ->
                                    add(instance)
                                }
                            }
                        }
                        fixAndWrite(existingInstances, true)
                    },
                    "index" to Int::class,
                    "instance" to PyObject::class
                )
                .build()
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

    private fun fixAndWrite() {
        val instances = getInstances()
        fixAndWrite(instances)
    }

    private fun fixAndWrite(instance: JsonArray?) {
        fixAndWrite(instance, false)
    }

    private fun fixAndWrite(instances: JsonArray?, forceWrite: Boolean?) {
        var updateCount = 0

        instances?.forEachIndexed { index, instance ->
            var viewParams = instance.asJsonObject?.get("viewParams") as? JsonObject
            if (viewParams == null) {
                viewParams = JsonObject()
            }
            updateCount += updateKey(index, instance.asJsonObject)
            updateCount += updateIndex(index, viewParams)
            instance.asJsonObject.add("viewParams", viewParams)
        }

        if (updateCount > 0 || forceWrite == true) {
            logger.trace("${component.componentAddressPath} - performing instance fix-up")
            props.queue.submit {
                props.write("instances", instances, Origin.Delegate, this)
                directlyUpdateViewIndexes()
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

    private fun directlyUpdateViewIndexes() {
        val page = component.page as PageModel

        val instances = getInstances()
        val keys = instances?.map { it.asJsonObject.get("key").asString }
        val views =
            keys?.map { key ->
                page.views.find { view ->
                    view.qualifiedPath.contains("${component.componentAddressPath}.$key")
                }
            }

        views?.forEachIndexed { index, view ->
            val params = view?.getPropertyTreeOf(PropertyType.params)
            params?.queue?.submitToHead {
                params.write("index", BasicQualifiedValue(index), Origin.Delegate, this)
            }
        }
    }

    override fun onStartup() {
        logger.debug("Model Delegate starting for ${component.componentAddressPath}")
        props = component.getPropertyTreeOf(PropertyType.props)!!
        fixAndWrite()
        directlyUpdateViewIndexes()

        instancesListener = props.subscribe("instances", Origin.ANY) { fixAndWrite() }
    }

    override fun onShutdown() {
        logger.debug("Model Delegate stopping for ${component.componentAddressPath}")
        instancesListener.unsubscribe()
    }
}
