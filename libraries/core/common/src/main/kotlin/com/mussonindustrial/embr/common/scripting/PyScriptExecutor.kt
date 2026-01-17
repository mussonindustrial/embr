package com.mussonindustrial.embr.common.scripting

import com.inductiveautomation.ignition.common.script.JythonExecException
import com.inductiveautomation.ignition.common.script.ScriptManager
import org.python.core.Py
import org.python.core.PyException
import org.python.core.PyObject

interface PyScriptExecutor {
    fun run(func: PyObject, vararg args: PyObject): PyObject?
}

fun ScriptManager.asPyScriptExecutor(): PyScriptExecutor =
    object : PyScriptExecutor {
        override fun run(func: PyObject, args: Array<out PyObject>): PyObject? {
            return try {
                this@asPyScriptExecutor.runFunction(func, *args)
            } catch (e: JythonExecException) {
                throw (e.pyCause.orElseGet { Py.JavaError(e) } as PyException)
            }
        }
    }
