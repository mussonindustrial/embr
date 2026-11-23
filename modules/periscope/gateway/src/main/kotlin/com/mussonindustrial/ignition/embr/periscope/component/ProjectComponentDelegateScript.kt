package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.script.ScriptManager
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource.Descriptor.DELEGATE_FACTORY
import org.python.core.CompileMode
import org.python.core.CompilerFlags
import org.python.core.Py
import org.python.core.PyCode
import org.python.core.PyObject

class ProjectComponentDelegateScript(val path: String, source: String) {

    private val code: PyCode =
        Py.compile_flags(source, "<delegate:$path>", CompileMode.exec, CompilerFlags())

    fun create(scriptManager: ScriptManager, component: PyObject): PyObject? {
        val scope = scriptManager.createLocalsMap()
        scriptManager.runCode(code, scope, scope)

        val factory =
            scope.__finditem__(DELEGATE_FACTORY)
                ?: error("The delegate of '$path' must define '$DELEGATE_FACTORY'.")
        return scriptManager.runFunction(factory, component).takeUnless { it == Py.None }
    }
}
