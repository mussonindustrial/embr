package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.binding.BindingUtils
import com.inductiveautomation.perspective.gateway.binding.BindingUtils.toJsonDeep
import com.mussonindustrial.embr.perspective.gateway.component.JavaScriptProxyableComponentModelDelegate
import com.mussonindustrial.embr.perspective.gateway.javascript.JavaScriptProxy
import org.python.core.PySequence

class SmoothieChartModelDelegate(component: Component) :
    JavaScriptProxyableComponentModelDelegate(component) {

    inner class ProxyWrapper(val proxy: JavaScriptProxy) : JavaScriptProxy by proxy {
        @Suppress("unused")
        fun appendData(values: PySequence) {
            val json =
                toJsonDeep(
                    TypeUtilities.pyToJava(values),
                    BindingUtils.JsonEncoding.DollarQualified,
                )
            this@SmoothieChartModelDelegate.fireEvent(
                "data-append",
                JsonObject().apply { add("values", json) },
            )
        }
    }

    override fun getJavaScriptProxy(): JavaScriptProxy {
        val proxy = super.getJavaScriptProxy()
        return ProxyWrapper(proxy)
    }
}
