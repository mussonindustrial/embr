package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.mussonindustrial.ignition.embr.periscope.Meta
import org.python.core.PyDictionary
import org.python.core.PyFunction
import org.python.core.PyObject

interface PerspectiveExtensions {

    @KeywordArgs(
        names = ["function", "args"],
        types = [String::class, PyDictionary::class],
    )
    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    fun runJavaScriptBlocking(args: Array<PyObject>, keywords: Array<String>): Any?

    @KeywordArgs(
        names = ["function", "args", "callback"],
        types = [String::class, PyDictionary::class, PyFunction::class],
    )
    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    fun runJavaScriptAsync(args: Array<PyObject>, keywords: Array<String>)
}
