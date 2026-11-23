package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate
import com.inductiveautomation.perspective.gateway.messages.EventFiredMsg
import com.inductiveautomation.perspective.gateway.model.ComponentModel
import com.mussonindustrial.embr.perspective.gateway.component.JavaScriptProxyableComponentModelDelegate
import java.util.AbstractSet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.python.core.Py
import org.python.core.PyObject

class ProjectComponentModelDelegate(
    component: Component,
    private val defaultsManager: ProjectComponentDefaultsManager,
    private val delegateScriptManager: ProjectComponentDelegateScriptManager,
) : JavaScriptProxyableComponentModelDelegate(component) {

    companion object {
        private val SCRIPT_CALLABLE_METHODS =
            ComponentModelDelegate::class.java.getDeclaredField("scriptCallableMethods").apply {
                isAccessible = true
            }
        private val JAVASCRIPT_PROXY_MESSAGES =
            setOf(
                JavaScriptProxyableComponentModelDelegate.MESSAGE_RESOLVE,
                JavaScriptProxyableComponentModelDelegate.MESSAGE_ERROR,
            )
    }

    private val session = component.session
    private val queue = session.queue()
    private val instance = AtomicReference<ProjectComponentDelegateInstance?>()
    private val started = AtomicBoolean(false)
    private val scriptsListener: () -> Unit = {
        if (instance.get() != null || findScript() != null) {
            queue.submit { restartDelegate() }
        }
    }

    init {
        defaultsManager.fillProps(component)
        @Suppress("UNCHECKED_CAST") val builtIn = SCRIPT_CALLABLE_METHODS.get(this) as Set<String>
        SCRIPT_CALLABLE_METHODS.set(this, ScriptCallableMethods(builtIn))
    }

    override fun onStartup() {
        component.mdc { log.debugf("Startup") }
        defaultsManager.fillPositions(component)
        delegateScriptManager.addListener(session.projectName, scriptsListener)
        started.set(true)
        startupDelegate()
    }

    override fun onShutdown() {
        component.mdc { log.debugf("Shutdown") }
        delegateScriptManager.removeListener(session.projectName, scriptsListener)
        started.set(false)
        shutdownDelegate()
    }

    override fun handleEvent(message: EventFiredMsg) {
        component.mdc { log.tracef("Received '%s' component message.", message.eventName) }
        if (message.eventName in JAVASCRIPT_PROXY_MESSAGES) {
            super.handleEvent(message)
            return
        }

        instance.get()?.handleEvent(message.eventName, message.event ?: JsonObject())
    }

    /** Jython looks up script-callable methods here, on behalf of component scripts. */
    @Suppress("FunctionName")
    fun __getattr__(name: String): PyObject {
        return findScriptCallable(name) ?: throw Py.AttributeError(name)
    }

    private fun findScript(): ProjectComponentDelegateScript? {
        return delegateScriptManager.find(
            session.projectName,
            ProjectComponents.getId(component.type),
        )
    }

    private fun startupDelegate() {
        val script = findScript() ?: return
        val created = ProjectComponentDelegateInstance(component as ComponentModel, script)
        instance.set(created)
        created.startup()
    }

    private fun shutdownDelegate() {
        instance.getAndSet(null)?.shutdown()
    }

    private fun restartDelegate() {
        if (!started.get()) {
            return
        }

        shutdownDelegate()
        startupDelegate()
    }

    private fun findScriptCallable(name: String): PyObject? {
        return instance.get()?.findScriptCallable(name)
    }

    private inner class ScriptCallableMethods(private val builtIn: Set<String>) :
        AbstractSet<String>() {
        override val size: Int
            get() = builtIn.size

        override fun iterator(): MutableIterator<String> {
            return builtIn.toMutableSet().iterator()
        }

        override fun contains(element: String): Boolean {
            return element in builtIn || findScriptCallable(element) != null
        }
    }
}
