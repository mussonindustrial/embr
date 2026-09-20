package com.mussonindustrial.ignition.embr.periscope.pystore

import com.mussonindustrial.ignition.embr.periscope.utils.toJavaValue
import com.mussonindustrial.ignition.embr.periscope.utils.toPyValue
import org.python.core.Py
import org.python.core.PyList
import org.python.core.PyObject
import org.python.core.PyTuple

abstract class PyStoreNode : PyObject() {

    internal abstract val owner: PyStore

    abstract val path: PyStorePath

    @JvmOverloads
    fun get(relativePath: String, default: Any? = null): Any? {
        return when (val result = owner.read(resolve(relativePath))) {
            is PyStoreReadResult.Resolved -> result.value
            PyStoreReadResult.Unresolved -> default
        }
    }

    fun getOrPut(relativePath: String, initializer: PyObject): Any? {
        require(relativePath.isNotEmpty()) {
            "Cannot replace a PyStoreNode through itself"
        }

        return owner.getOrPut(resolve(relativePath)) {
            initializer.__call__().toJavaValue()
        }
    }

    fun set(relativePath: String, value: Any?) {
        require(relativePath.isNotEmpty()) {
            "Cannot replace a PyStoreNode through itself"
        }

        owner.set(resolve(relativePath), value)
    }

    fun contains(relativePath: String): Boolean = owner.contains(resolve(relativePath))

    fun remove(relativePath: String): Any? {
        require(relativePath.isNotEmpty()) {
            "Cannot remove a PyStoreNode through itself"
        }

        return when (val result = owner.remove(resolve(relativePath))) {
            is PyStoreReadResult.Resolved -> result.value
            PyStoreReadResult.Unresolved -> null
        }
    }

    fun clear() {
        owner.clear(path)
    }

    fun node(relativePath: String): PyStoreNode {
        if (relativePath.isEmpty()) return this

        return owner.node(resolve(relativePath))
    }

    fun touch() {
        owner.touch(path)
    }

    fun touch(relativePath: String) {
        owner.touch(resolve(relativePath))
    }

    fun touchMany(vararg relativePaths: String) {
        owner.touchMany(relativePaths.map(::resolve))
    }

    override fun __findattr_ex__(name: String): PyObject? {
        super.__findattr_ex__(name)?.let {
            return it
        }

        return when (val result = owner.read(path.child(name))) {
            is PyStoreReadResult.Resolved -> result.value.toPyValue()
            PyStoreReadResult.Unresolved -> null
        }
    }

    override fun __setattr__(name: String, value: PyObject) {
        if (name.startsWith("__")) {
            super.__setattr__(name, value)
            return
        }

        if (super.__findattr_ex__(name) != null) {
            throw Py.AttributeError(
                "'$name' is reserved on PyStoreNode; use ['${name}'] to set a state key with that name"
            )
        }

        owner.set(path.child(name), value.toJavaValue())
    }

    override fun __finditem__(key: String): PyObject? {
        return when (val result = owner.read(path.child(key))) {
            is PyStoreReadResult.Resolved -> result.value.toPyValue()
            PyStoreReadResult.Unresolved -> null
        }
    }

    override fun __setitem__(key: PyObject, value: PyObject) {
        owner.set(
            path.child(key.asString()),
            value.toJavaValue(),
        )
    }

    override fun __delitem__(key: PyObject) {
        val name = key.asString()

        when (owner.remove(path.child(name))) {
            is PyStoreReadResult.Resolved -> Unit
            PyStoreReadResult.Unresolved -> throw Py.KeyError(key)
        }
    }

    override fun __contains__(key: PyObject): Boolean = owner.contains(path.child(key.asString()))

    override fun __len__(): Int = owner.children(path).size

    override fun __iter__(): PyObject = keys().__iter__()

    fun keys(): PyList = PyList(owner.children(path).map { Py.newString(it.first) })

    fun values(): PyList = PyList(owner.children(path).map { it.second.toPyValue() })

    fun items(): PyList =
        PyList(
            owner.children(path).map { (key, value) ->
                PyTuple(Py.newString(key), value.toPyValue())
            }
        )

    protected fun resolve(relativePath: String): PyStorePath =
        path.resolve(PyStorePath.parse(relativePath))

    override fun toString(): String =
        if (path.isRoot) {
            "PyStoreNode(${owner.name})"
        } else {
            "PyStoreNode(${owner.name}:$path)"
        }
}
