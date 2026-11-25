package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.JsonElement
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.perspective.common.api.PropertyKey
import com.inductiveautomation.perspective.common.config.PropertyConfig
import com.inductiveautomation.perspective.common.property.Origin
import com.inductiveautomation.perspective.gateway.api.ExtensionFunction
import com.inductiveautomation.perspective.gateway.model.ComponentModel
import com.inductiveautomation.perspective.gateway.property.PropertyTree
import com.inductiveautomation.perspective.gateway.script.ComponentModelScriptWrapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.optionals.getOrNull
import org.python.core.Py
import org.python.core.PyDictionary
import org.python.core.PyObject

class ProjectComponentScriptWrapper(
    private val component: ComponentModel,
    private val runHandler: (handler: PyObject, args: Array<PyObject>) -> Unit,
) : ComponentModelScriptWrapper(component) {

    companion object {
        private const val EVENT_SCOPE = "model"
        private val EXTENSION_NAMES =
            setOf(
                "fireEvent",
                "onPropertyChange",
                "type",
                "addressPath",
                "getPropertyConfig",
                "findExtensionFunction",
            )

        private fun toPython(value: QualifiedValue?): PyObject {
            if (value == null) {
                return Py.None
            }

            val content = value.value
            if (content !is JsonElement) {
                return Py.java2py(value)
            }

            return Py.java2py(
                BasicQualifiedValue(
                    TypeUtilities.gsonToPy(content),
                    value.quality,
                    value.timestamp,
                ),
            )
        }
    }

    private val extensions = Py.java2py(Extensions())
    private val subscriptions = ConcurrentHashMap.newKeySet<PropertyTree.Subscription>()

    override fun __findattr_ex__(name: String): PyObject? {
        if (name in EXTENSION_NAMES) {
            return extensions.__findattr__(name)
        }

        return super.__findattr_ex__(name)
    }

    fun shutdown() {
        subscriptions.toList().forEach { it.unsubscribe() }
    }

    private inner class PropertySubscription(
        private val subscription: PropertyTree.Subscription,
        private val active: AtomicBoolean,
    ) : PropertyTree.Subscription {

        override fun unsubscribe() {
            if (subscriptions.remove(this)) {
                active.set(false)
                subscription.unsubscribe()
            }
        }
    }

    inner class Extensions {

        val type: String
            get() = component.type

        val addressPath: String
            get() = component.componentAddressPath

        fun fireEvent(name: String) {
            fireEvent(name, PyDictionary())
        }

        fun fireEvent(name: String, event: PyObject) {
            val json = TypeUtilities.pyToGson(event)
            if (json !is JsonObject) {
                throw Py.TypeError("The event must be a dictionary.")
            }

            component.fireEvent(EVENT_SCOPE, name, json)
        }

        fun onPropertyChange(path: String, handler: PyObject): PropertyTree.Subscription {
            val key = PropertyKey.fromString(path)
            val tree =
                component.getPropertyTreeOf(key.scope)
                    ?: throw Py.ValueError("Cannot listen to '$path', it has no property tree.")

            val active = AtomicBoolean(true)
            val previous = AtomicReference(tree.read(key.path).getOrNull())
            val treeSubscription =
                tree.subscribe(key.path, Origin.ANY) { event ->
                    val current = event.readValue()
                    val args =
                        arrayOf(
                            toPython(previous.getAndSet(current)),
                            toPython(current),
                            Py.newUnicode(event.origin.name),
                        )

                    component.session.queue().submit {
                        if (active.get()) {
                            runHandler(handler, args)
                        }
                    }
                }

            val subscription = PropertySubscription(treeSubscription, active)
            subscriptions += subscription
            return subscription
        }

        fun getPropertyConfig(path: String): PropertyConfig? {
            return component.getPropertyConfig(PropertyKey.fromString(path)).getOrNull()
        }

        fun findExtensionFunction(name: String): ExtensionFunction? {
            return component.findExtensionFunction(name).getOrNull()
        }
    }
}
