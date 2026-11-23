package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.gateway.model.ComponentModel
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.model.threadContext
import com.mussonindustrial.embr.perspective.gateway.model.withThreadContext
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource.Descriptor.DELEGATE_FACTORY
import org.python.core.Py
import org.python.core.PyObject

/** One run of a component's delegate script, from its creation to its shutdown. */
class ProjectComponentDelegateInstance(
    private val component: ComponentModel,
    script: ProjectComponentDelegateScript,
) {

    companion object {
        private const val ON_STARTUP = "onStartup"
        private const val ON_SHUTDOWN = "onShutdown"
        private const val HANDLE_EVENT = "handleEvent"
        private const val ON_PROPERTY_CHANGE = "onPropertyChange"
        private val LIFECYCLE_METHODS = setOf(ON_STARTUP, ON_SHUTDOWN, HANDLE_EVENT)
    }

    private val logger = this.getLoggerEx()
    private val scriptManager = component.session.scriptManager
    private val wrapper =
        ProjectComponentScriptWrapper(component) { handler, args ->
            run(ON_PROPERTY_CHANGE) { scriptManager.runFunction(handler, *args) }
        }

    private val delegate: PyObject? = create(script)

    fun startup() {
        call(ON_STARTUP)
    }

    fun shutdown() {
        call(ON_SHUTDOWN)
        wrapper.shutdown()
    }

    fun handleEvent(name: String, event: JsonObject) {
        call(HANDLE_EVENT, Py.newUnicode(name), TypeUtilities.gsonToPy(event))
    }

    /** A method component scripts can call: public, and not part of the lifecycle. */
    fun findScriptCallable(name: String): PyObject? {
        if (name.startsWith("_") || name in LIFECYCLE_METHODS) {
            return null
        }

        return findMethod(name)
    }

    private fun findMethod(name: String): PyObject? {
        return delegate?.__findattr__(name)?.takeIf { it.isCallable }
    }

    private fun call(name: String, vararg args: PyObject) {
        val method = findMethod(name) ?: return
        run(name) { scriptManager.runFunction(method, *args) }
    }

    private fun create(script: ProjectComponentDelegateScript): PyObject? {
        var created: PyObject? = null
        run(DELEGATE_FACTORY) { created = script.create(scriptManager, wrapper) }
        return created
    }

    private fun run(name: String, block: () -> Unit) {
        try {
            withThreadContext(component.threadContext, block)
        } catch (e: Exception) {
            component.mdc { logger.warn("Failed to run '$name' of the component's delegate.", e) }
        }
    }
}
