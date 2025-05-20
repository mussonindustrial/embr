package com.mussonindustrial.embr.perspective.gateway.javascript

import com.inductiveautomation.perspective.gateway.api.ScriptCallable

interface JavaScriptProxyable {

    @ScriptCallable
    fun getJavaScriptProxy(): JavaScriptProxy

    @ScriptCallable
    @Deprecated("legacy overload", ReplaceWith("getJavaScriptProxy()"))
    fun getJavaScriptProxy(property: String): JavaScriptProxy {
        return getJavaScriptProxy()
    }
}
