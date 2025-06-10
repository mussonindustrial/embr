package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.internal.Streams
import com.inductiveautomation.ignition.common.gson.stream.JsonWriter
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap
import com.inductiveautomation.ignition.gateway.dataroutes.RouteGroup
import com.inductiveautomation.perspective.gateway.api.*
import com.inductiveautomation.perspective.gateway.api.FetchableCache.Fetchable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicBoolean
import javax.servlet.http.HttpServletResponse
import org.python.core.Py
import org.python.core.PyDictionary
import org.python.core.PyObject

class ApexChartsLegacyModelDelegate(component: Component) : ComponentModelDelegate(component) {
    private val toggleSeriesWaiting = AtomicBoolean(false)
    private val toggleSeriesReturn = AtomicBoolean(false)

    private val context: PerspectiveContext = component.session.perspectiveContext

    override fun onStartup() {
        // Called when the Gateway's ComponentModel starts.  The start itself happens when the
        // client project is
        // loading and includes an instance of the the component type in the page/view being
        // started.
        log.debugf("Starting up delegate for '%s'!", component.componentAddressPath)
    }

    override fun onShutdown() {
        // Called when the component is removed from the page/view and the model is shutting down.
        log.debugf("Shutting down delegate for '%s'!", component.componentAddressPath)
    }

    @ScriptCallable
    @KeywordArgs(names = ["seriesName"], types = [String::class])
    @Throws(Exception::class)
    fun toggleSeries(pyArgs: Array<PyObject?>?, keywords: Array<String?>?): Boolean {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "toggleSeries",
            )
        val seriesName =
            argumentMap.getStringArg("seriesName")
                ?: throw Py.ValueError("toggleSeries argument 'seriesName' cannot be None")

        toggleSeriesWaiting.set(true)
        log.debugf("Calling toggleSeries with '%s'", seriesName)
        val payload = JsonObject()
        payload.addProperty("functionToCall", "toggleSeries")
        payload.addProperty("seriesName", seriesName)
        fireEvent(OUTBOUND_EVENT_NAME, payload)

        val maxTryCount = 20
        var tryCount = 0
        while (toggleSeriesWaiting.get()) {
            tryCount += 1
            if (tryCount >= maxTryCount) {
                toggleSeriesWaiting.set(false)
                throw Exception("No message received from ApexChart, failing")
            }
            Thread.sleep(100)
        }

        toggleSeriesWaiting.set(false)
        return toggleSeriesReturn.get()
    }

    @ScriptCallable
    @KeywordArgs(names = ["seriesName"], types = [String::class])
    @Throws(Exception::class)
    fun showSeries(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "showSeries",
            )
        val seriesName =
            argumentMap.getStringArg("seriesName")
                ?: throw Py.ValueError("showSeries argument 'seriesName' cannot be None")

        log.debugf("Calling showSeries with '%s'", seriesName)
        val payload = JsonObject()
        payload.addProperty("functionToCall", "showSeries")
        payload.addProperty("seriesName", seriesName)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(names = ["seriesName"], types = [String::class])
    @Throws(Exception::class)
    fun hideSeries(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "hideSeries",
            )
        val seriesName =
            argumentMap.getStringArg("seriesName")
                ?: throw Py.ValueError("hideSeries argument 'seriesName' cannot be None")

        log.debugf("Calling hideSeries with '%s'", seriesName)
        val payload = JsonObject()
        payload.addProperty("functionToCall", "hideSeries")
        payload.addProperty("seriesName", seriesName)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["shouldUpdateChart", "shouldResetZoom"],
        types = [Boolean::class, Boolean::class],
    )
    @Throws(Exception::class)
    fun resetSeries(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "resetSeries",
            )
        val shouldUpdateChart = argumentMap.getBooleanArg("shouldUpdateChart", true)
        val shouldResetZoom = argumentMap.getBooleanArg("shouldResetZoom", true)

        log.debugf(
            "Calling resetSeries with shouldUpdateChart=%s and shouldResetZoom=%s",
            shouldUpdateChart,
            shouldResetZoom,
        )
        val payload = JsonObject()
        payload.addProperty("functionToCall", "resetSeries")
        payload.addProperty("shouldUpdateChart", shouldUpdateChart)
        payload.addProperty("shouldResetZoom", shouldResetZoom)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(names = ["start", "end"], types = [Long::class, Long::class])
    @Throws(Exception::class)
    fun zoomX(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "zoomX",
            )
        val start = argumentMap.getLongArg("start")
        val end = argumentMap.getLongArg("end")

        log.debugf("Calling zoomX with start=%s and end=%s", start, end)
        val payload = JsonObject()
        payload.addProperty("functionToCall", "zoomX")
        payload.addProperty("start", start)
        payload.addProperty("end", end)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(names = ["options", "pushToMemory"], types = [PyDictionary::class, Boolean::class])
    @Throws(Exception::class)
    fun addPointAnnotation(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "addPointAnnotation",
            )
        val options = argumentMap["options"] as PyDictionary?
        val pushToMemory = argumentMap.getBooleanArg("pushToMemory", true)

        val gson = Gson()
        log.debug("Calling addPointAnnotation")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "addPointAnnotation")
        payload.add("options", gson.toJsonTree(options))
        payload.addProperty("pushToMemory", pushToMemory)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @Throws(Exception::class)
    fun clearAnnotations() {
        log.debug("Calling clearAnnotations")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "clearAnnotations")
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["newSeries", "animate", "maintainZoom", "syncProps", "fetchResults"],
        types = [MutableList::class, Boolean::class, Boolean::class, Boolean::class, Boolean::class],
    )
    @Throws(Exception::class)
    fun updateSeries(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "updateSeries",
            )
        val newSeries = argumentMap["newSeries"] as List<*>?
        val animate = argumentMap.getBooleanArg("animate", true)
        val maintainZoom = argumentMap.getBooleanArg("maintainZoom", false)
        var syncProps = argumentMap.getBooleanArg("syncProps", false)
        val fetchResults = argumentMap.getBooleanArg("fetchResults", false)

        var url: String? = null
        val gson = Gson()
        val payload = JsonObject()
        val json = gson.toJsonTree(newSeries)

        if (fetchResults!!) {
            syncProps = false

            val session: Session = component.session
            val fetchableCache = context.fetchableCache
            url = fetchableCache.addFetchable(session, ApexDataFetch(session, json))
        }

        log.debug("Calling updateSeries")
        payload.addProperty("functionToCall", "updateSeries")
        payload.add("newSeries", json)
        payload.addProperty("animate", animate)
        payload.addProperty("maintainZoom", maintainZoom)
        payload.addProperty("syncProps", syncProps)
        payload.addProperty("fetchResults", fetchResults)
        if (fetchResults) {
            payload.addProperty("url", url)
        }
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(
        names =
            [
                "newOptions",
                "redrawPaths",
                "animate",
                "updateSyncedCharts",
                "maintainZoom",
                "syncProps",
            ],
        types =
            [
                PyDictionary::class,
                Boolean::class,
                Boolean::class,
                Boolean::class,
                Boolean::class,
                Boolean::class,
            ],
    )
    @Throws(Exception::class)
    fun updateOptions(pyArgs: Array<PyObject?>?, keywords: Array<String?>?) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "updateOptions",
            )
        val newOptions = argumentMap["newOptions"] as PyDictionary?
        val redrawPaths = argumentMap.getBooleanArg("redrawPaths", false)
        val animate = argumentMap.getBooleanArg("animate", true)
        val updateSyncedCharts = argumentMap.getBooleanArg("updateSyncedCharts", true)
        val maintainZoom = argumentMap.getBooleanArg("maintainZoom", false)
        val syncProps = argumentMap.getBooleanArg("syncProps", false)

        val gson = Gson()
        log.debug("Calling updateOptions")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "updateOptions")
        payload.add("newOptions", gson.toJsonTree(newOptions))
        payload.addProperty("redrawPaths", redrawPaths)
        payload.addProperty("animate", animate)
        payload.addProperty("updateSyncedCharts", updateSyncedCharts)
        payload.addProperty("maintainZoom", maintainZoom)
        payload.addProperty("syncProps", syncProps)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    // when a ComponentStoreDelegate event is fired from the client side, it comes through this
    // method.
    override fun handleEvent(message: EventFiredMsg) {
        log.debugf("Received EventFiredMessage of type: %s", message.eventName)

        if (message.eventName == INBOUND_EVENT_NAME) {
            val payload = message.event
            toggleSeriesReturn.set(payload["result"].asBoolean)
            toggleSeriesWaiting.set(false)
        }
    }

    private inner class ApexDataFetch(session: Session, val json: JsonElement) : Fetchable {

        @Throws(IOException::class)
        override fun fetch(response: HttpServletResponse) {
            response.contentType = RouteGroup.TYPE_JSON
            response.characterEncoding = UTF_8.name()
            OutputStreamWriter(response.outputStream, UTF_8).use { streamWriter ->
                JsonWriter(streamWriter).use { jsonWriter -> Streams.write(json, jsonWriter) }
            }
        }
    }

    companion object {
        const val OUTBOUND_EVENT_NAME: String = "apexchart-response-event"
        const val INBOUND_EVENT_NAME: String = "apexchart-request-event"
    }
}
