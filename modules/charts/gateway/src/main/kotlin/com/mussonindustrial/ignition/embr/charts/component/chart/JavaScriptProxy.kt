package com.mussonindustrial.ignition.embr.charts.component.chart

import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import org.python.core.PyDictionary
import org.python.core.PyObject

interface JavaScriptProxy {

    @KeywordArgs(
        names = ["function", "args"],
        types = [String::class, PyDictionary::class, String::class, String::class],
    )
    fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>): Any?

    @KeywordArgs(
        names = ["function", "args", "callback"],
        types = [String::class, PyDictionary::class, String::class, String::class],
    )
    fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>)
}
