package com.mussonindustrial.ignition.embr.periscope.pystore

import com.mussonindustrial.ignition.embr.periscope.utils.toJavaValue
import com.mussonindustrial.ignition.embr.periscope.utils.toPyValue
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Array
import java.util.concurrent.ConcurrentHashMap
import org.python.core.Py
import org.python.core.PyObject
import org.python.core.PySequence

class PyStoreValueResolver {

    private val beanProperties = ConcurrentHashMap<Class<*>, Map<String, PropertyDescriptor>>()

    fun read(
        target: Any,
        segment: String,
    ): PyStoreReadResult {
        val index = segment.toIntOrNull()

        return when {
            target is Map<*, *> ->
                if (target.containsKey(segment)) {
                    resolved(target[segment])
                } else {
                    unresolved
                }

            target is PySequence ->
                if (index != null) {
                    target.__finditem__(index)?.let { resolved(it.toJavaValue()) } ?: unresolved
                } else {
                    readAttribute(target, segment)
                }

            target is List<*> ->
                if (index != null && index in target.indices) {
                    resolved(target[index])
                } else {
                    unresolved
                }

            target.javaClass.isArray ->
                if (index != null && index in 0 until Array.getLength(target)) {
                    resolved(Array.get(target, index))
                } else {
                    unresolved
                }

            target is PyObject -> readAttribute(target, segment)

            else ->
                property(target, segment)?.readMethod?.let {
                    resolved(it.invoke(target))
                } ?: unresolved
        }
    }

    fun write(
        target: Any,
        segment: String,
        value: Any?,
    ) {
        val index = segment.toIntOrNull()

        when {
            target is Map<*, *> -> put(target, segment, value)

            target is PySequence ->
                if (index != null) {
                    requireIndex(target, index)
                    target.__setitem__(index, value.toPyValue())
                } else {
                    target.__setattr__(segment.intern(), value.toPyValue())
                }

            target is List<*> -> set(target, requireIndex(target, segment, target.size), value)

            target.javaClass.isArray ->
                Array.set(target, requireIndex(target, segment, Array.getLength(target)), value)

            target is PyObject -> target.__setattr__(segment.intern(), value.toPyValue())

            else -> {
                val descriptor =
                    property(target, segment)
                        ?: throw IllegalArgumentException(
                            "${typeName(target)} has no property '$segment'"
                        )

                val setter =
                    descriptor.writeMethod
                        ?: throw IllegalArgumentException(
                            "Property '$segment' on ${typeName(target)} is read-only"
                        )

                setter.invoke(target, value)
            }
        }
    }

    fun remove(
        target: Any,
        segment: String,
    ): PyStoreReadResult {
        val index = segment.toIntOrNull()

        return when {
            target is Map<*, *> -> remove(target, segment)

            target is PySequence ->
                if (index != null) {
                    val previous = target.__finditem__(index) ?: return unresolved

                    target.__delitem__(Py.newInteger(index))
                    resolved(previous.toJavaValue())
                } else {
                    removeAttribute(target, segment)
                }

            target is List<*> ->
                if (index != null && index in target.indices) {
                    resolved(remove(target, index))
                } else {
                    unresolved
                }

            target.javaClass.isArray ->
                if (index != null && index in 0 until Array.getLength(target)) {
                    throw IllegalArgumentException(
                        "Cannot remove elements from ${typeName(target)}"
                    )
                } else {
                    unresolved
                }

            target is PyObject -> removeAttribute(target, segment)

            property(target, segment) != null ->
                throw IllegalArgumentException(
                    "Cannot remove property '$segment' from ${typeName(target)}"
                )

            else -> unresolved
        }
    }

    private fun readAttribute(
        target: PyObject,
        name: String,
    ): PyStoreReadResult =
        target.__findattr_ex__(name.intern())?.let { resolved(it.toJavaValue()) } ?: unresolved

    private fun removeAttribute(
        target: PyObject,
        name: String,
    ): PyStoreReadResult {
        val interned = name.intern()
        val previous = target.__findattr_ex__(interned) ?: return unresolved

        target.__delattr__(interned)

        return resolved(previous.toJavaValue())
    }

    private fun put(
        target: Map<*, *>,
        key: String,
        value: Any?,
    ) {
        mutate("${typeName(target)} is read-only") {
            @Suppress("UNCHECKED_CAST")
            (target as MutableMap<Any?, Any?>)[key] = value
        }
    }

    private fun remove(
        target: Map<*, *>,
        key: String,
    ): PyStoreReadResult {
        if (!target.containsKey(key)) {
            return unresolved
        }

        val previous = target[key]

        mutate("${typeName(target)} is read-only") {
            @Suppress("UNCHECKED_CAST") (target as MutableMap<Any?, Any?>).remove(key)
        }

        return resolved(previous)
    }

    private fun set(
        target: List<*>,
        index: Int,
        value: Any?,
    ) {
        mutate("${typeName(target)} is read-only") {
            @Suppress("UNCHECKED_CAST")
            (target as MutableList<Any?>)[index] = value
        }
    }

    private fun remove(
        target: List<*>,
        index: Int,
    ): Any? =
        mutate("${typeName(target)} does not support removing elements") {
            @Suppress("UNCHECKED_CAST") (target as MutableList<Any?>).removeAt(index)
        }

    private fun requireIndex(
        target: Any,
        segment: String,
        size: Int,
    ): Int {
        val index =
            segment.toIntOrNull()
                ?: throw IllegalArgumentException(
                    "'$segment' is not a valid index for ${typeName(target)}"
                )

        requireIndex(target, index, size)
        return index
    }

    private fun requireIndex(
        target: PySequence,
        index: Int,
    ) {
        if (target.__finditem__(index) == null) {
            throw IndexOutOfBoundsException("Index $index is out of range for ${typeName(target)}")
        }
    }

    private fun requireIndex(
        target: Any,
        index: Int,
        size: Int,
    ) {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException("Index $index is out of range for ${typeName(target)}")
        }
    }

    private fun property(
        target: Any,
        name: String,
    ): PropertyDescriptor? = properties(target.javaClass)[name]

    private fun properties(type: Class<*>): Map<String, PropertyDescriptor> =
        beanProperties.computeIfAbsent(type) {
            Introspector.getBeanInfo(it)
                .propertyDescriptors
                .filterNot { property ->
                    property.name == "class"
                }
                .associateBy(PropertyDescriptor::getName)
        }

    private inline fun <T> mutate(
        message: String,
        operation: () -> T,
    ): T =
        try {
            operation()
        } catch (exception: UnsupportedOperationException) {
            throw IllegalArgumentException(
                message,
                exception,
            )
        }

    private fun typeName(value: Any): String =
        value.javaClass.simpleName.takeIf(String::isNotBlank) ?: value.javaClass.name

    private fun resolved(value: Any?): PyStoreReadResult = PyStoreReadResult.Resolved(value)

    private val unresolved: PyStoreReadResult
        get() = PyStoreReadResult.Unresolved
}
