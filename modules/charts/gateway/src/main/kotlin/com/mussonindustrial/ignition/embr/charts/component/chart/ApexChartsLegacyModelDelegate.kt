package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.gson.Gson
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.api.ScriptCallable
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import java.util.concurrent.atomic.AtomicBoolean
import org.python.core.Py
import org.python.core.PyDictionary
import org.python.core.PyObject

class ApexChartsLegacyModelDelegate(component: Component?) : ComponentModelDelegate(component) {
    private val toggleSeriesWaiting = AtomicBoolean(false)
    private val toggleSeriesReturn = AtomicBoolean(false)

    override fun onStartup() {
        // Called when the Gateway's ComponentModel starts.  The start itself happens when the
        // client project is
        // loading and includes an instance of the component type in the page/view being started.
        log.debugf("Starting up delegate for '%s'!", component.componentAddressPath)
    }

    override fun onShutdown() {
        // Called when the component is removed from the page/view and the model is shutting down.
        log.debugf("Shutting down delegate for '%s'!", component.componentAddressPath)
    }

    @ScriptCallable
    @KeywordArgs(names = ["seriesName"], types = [String::class])
    @Throws(Exception::class)
    @Suppress("unused")
    fun toggleSeries(pyArgs: Array<PyObject?>, keywords: Array<String?>): Boolean {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "toggleSeries",
            )
        val seriesName = argumentMap.getStringArg("seriesName")

        if (seriesName == null) {
            throw Py.ValueError("toggleSeries argument 'seriesName' cannot be None")
        }

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
    @Suppress("unused")
    fun showSeries(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "showSeries",
            )
        val seriesName = argumentMap.getStringArg("seriesName")

        if (seriesName == null) {
            throw Py.ValueError("showSeries argument 'seriesName' cannot be None")
        }

        log.debugf("Calling showSeries with '%s'", seriesName)
        val payload = JsonObject()
        payload.addProperty("functionToCall", "showSeries")
        payload.addProperty("seriesName", seriesName)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(names = ["seriesName"], types = [String::class])
    @Throws(Exception::class)
    @Suppress("unused")
    fun hideSeries(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "hideSeries",
            )
        val seriesName = argumentMap.getStringArg("seriesName")

        if (seriesName == null) {
            throw Py.ValueError("hideSeries argument 'seriesName' cannot be None")
        }

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
    @Suppress("unused")
    fun resetSeries(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
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
    @Suppress("unused")
    fun zoomX(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
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
    @Suppress("unused")
    fun addPointAnnotation(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "addPointAnnotation",
            )
        val options = argumentMap.get("options") as PyDictionary?
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
    @Suppress("unused")
    fun clearAnnotations() {
        log.debug("Calling clearAnnotations")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "clearAnnotations")
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["newSeries", "animate", "maintainZoom"],
        types = [MutableList::class, Boolean::class, Boolean::class],
    )
    @Throws(Exception::class)
    @Suppress("unused")
    fun updateSeries(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
        val argumentMap =
            PyArgumentMap.interpretPyArgs(
                pyArgs,
                keywords,
                ApexChartsLegacyModelDelegate::class.java,
                "updateSeries",
            )
        val newSeries = argumentMap.get("newSeries") as MutableList<*>?
        val animate = argumentMap.getBooleanArg("animate", true)
        val maintainZoom = argumentMap.getBooleanArg("maintainZoom", false)

        val gson = Gson()
        log.debug("Calling updateSeries")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "updateSeries")
        payload.add("newSeries", gson.toJsonTree(newSeries))
        payload.addProperty("animate", animate)
        payload.addProperty("maintainZoom", maintainZoom)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    @ScriptCallable
    @KeywordArgs(
        names = ["newOptions", "redrawPaths", "animate", "updateSyncedCharts", "maintainZoom"],
        types =
            [PyDictionary::class, Boolean::class, Boolean::class, Boolean::class, Boolean::class],
    )
    @Throws(Exception::class)
    @Suppress("unused")
    fun updateOptions(pyArgs: Array<PyObject?>, keywords: Array<String?>) {
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

        val gson = Gson()
        log.debug("Calling updateOptions")
        val payload = JsonObject()
        payload.addProperty("functionToCall", "updateOptions")
        payload.add("newOptions", gson.toJsonTree(newOptions))
        payload.addProperty("redrawPaths", redrawPaths)
        payload.addProperty("animate", animate)
        payload.addProperty("updateSyncedCharts", updateSyncedCharts)
        payload.addProperty("maintainZoom", maintainZoom)
        fireEvent(OUTBOUND_EVENT_NAME, payload)
    }

    // when a ComponentStoreDelegate event is fired from the client side, it comes through this
    // method.
    override fun handleEvent(message: EventFiredMsg) {
        log.debugf("Received EventFiredMessage of type: %s", message.eventName)

        if (message.eventName == INBOUND_EVENT_NAME) {
            val payload = message.event
            toggleSeriesReturn.set(payload.get("result").asBoolean)
            toggleSeriesWaiting.set(false)
        }
    }

    companion object {
        const val OUTBOUND_EVENT_NAME: String = "apexchart-response-event"
        const val INBOUND_EVENT_NAME: String = "apexchart-request-event"
    }
}
