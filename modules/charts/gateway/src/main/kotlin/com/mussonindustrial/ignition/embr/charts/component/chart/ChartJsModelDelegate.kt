package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.JsonUtilities
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import kotlin.reflect.typeOf
import org.python.core.PyDictionary
import org.python.core.PyList
import org.python.core.PyObject

class ChartJsModelDelegate(component: Component) : ComponentModelDelegate(component) {

    private val log = LogUtil.getModuleLogger("embr-charts", "ChartJsModelDelegate")
    private val queue = component.session.queue()
    private val methods = MethodOverloads()

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["delta", "scales", "mode"],
        types = [Any::class, PyList::class, String::class],
    )
    @Suppress("unused")
    fun pan(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.pan.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["zoomLevel", "mode"],
        types = [PyObject::class, String::class],
    )
    @Suppress("unused")
    fun zoom(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.zoom.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["scaleId", "newRange", "mode"],
        types = [String::class, PyDictionary::class, String::class],
    )
    @Suppress("unused")
    fun zoomScale(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.zoomScale.call(args, keywords) } }

    @ScriptCallable
    @KeywordArgs(
        names = ["mode"],
        types = [String::class],
    )
    @Suppress("unused")
    fun resetZoom(args: Array<PyObject>, keywords: Array<String>) =
        queue.submit { component.mdc { methods.resetZoom.call(args, keywords) } }

    inner class MethodOverloads {
        val pan =
            PyArgOverloadBuilder()
                .setName("pan")
                .addOverload(
                    {
                        fireEvent(
                            "pan",
                            JsonObject().apply {
                                add("pan", JsonUtilities.javaToJson(it[0]))
                                add("scales", JsonUtilities.javaToJson(it[1]))
                                addProperty("scales", TypeUtilities.toString(it[2] ?: "none"))
                            }
                        )
                    },
                    "pan" to typeOf<Any>(),
                    "scales" to typeOf<PyList?>(),
                    "mode" to typeOf<String?>(),
                )
                .build()

        val zoom =
            PyArgOverloadBuilder()
                .setName("zoom")
                .addOverload(
                    {
                        val zoomLevel = TypeUtilities.pyToGson(it[0] as PyObject)
                        val mode = it[1] as? String ?: "none"

                        fireEvent(
                            "zoom",
                            JsonObject().apply {
                                add("zoomLevel", zoomLevel)
                                addProperty("mode", mode)
                            }
                        )
                    },
                    "zoomLevel" to typeOf<PyObject>(),
                    "mode" to typeOf<String?>(),
                )
                .build()

        val zoomScale =
            PyArgOverloadBuilder()
                .setName("zoomScale")
                .addOverload(
                    {
                        val scaleId = it[0] as String
                        val newRange = TypeUtilities.pyToGson(it[1] as PyObject)
                        val mode = it[2] as? String ?: "none"

                        fireEvent(
                            "zoomScale",
                            JsonObject().apply {
                                addProperty("id", scaleId)
                                add("range", newRange)
                                addProperty("mode", mode)
                            }
                        )
                    },
                    "id" to typeOf<String>(),
                    "range" to typeOf<PyDictionary>()
                )
                .build()

        val resetZoom =
            PyArgOverloadBuilder()
                .setName("resetZoom")
                .addOverload(
                    {
                        val mode = it[0] as String

                        fireEvent("resetZoom", JsonObject().apply { addProperty("mode", mode) })
                    },
                    "mode" to typeOf<String?>(),
                )
                .addOverload({
                    fireEvent("resetZoom", JsonObject().apply { addProperty("mode", "none") })
                })
                .build()
    }
}
