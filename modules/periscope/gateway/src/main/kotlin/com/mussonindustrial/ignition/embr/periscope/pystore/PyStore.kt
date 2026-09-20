package com.mussonindustrial.ignition.embr.periscope.pystore

import com.mussonindustrial.embr.common.logging.getLoggerEx
import org.python.core.ContextManager
import org.python.core.PyException
import org.python.core.PyObject
import org.python.core.ThreadState

class PyStore(
    val name: String,
    private val valueResolver: PyStoreValueResolver,
) : PyStoreNode() {

    private val logger = this.getLoggerEx()

    private val lock = Any()
    private val root = NodeData()

    private var active = true
    private var initialized = false
    private var initializing = false

    private var notificationDeferralDepth = 0

    private val changeset = PyStoreChangeset()
    private val subscribers = LinkedHashSet<ChangeListener>()

    override val owner: PyStore
        get() = this

    override val path: PyStorePath
        get() = PyStorePath.ROOT

    val isActive: Boolean
        get() = synchronized(lock) { active }

    val isInitialized: Boolean
        get() = synchronized(lock) { initialized }

    fun initialize(initializer: PyObject): Boolean {
        var attempted = false

        try {
            access {
                if (initialized) return false

                check(!initializing) { "PyStore '$name' is already being initialized" }

                initializing = true
                attempted = true

                try {
                    initializer.__call__(this)
                    initialized = true
                } finally {
                    initializing = false
                }
            }

            return true
        } finally {
            if (attempted) {
                recordRootChange()
            }
        }
    }

    fun deferNotifications(): PyObject = NotificationDeferral()

    fun subscribe(listener: ChangeListener): AutoCloseable {
        access { subscribers += listener }
        return AutoCloseable { synchronized(lock) { subscribers -= listener } }
    }

    internal fun read(path: PyStorePath): PyStoreReadResult = access {
        externalize(path, resolve(path.segments))
    }

    internal fun contains(path: PyStorePath): Boolean = access {
        resolve(path.segments) is PyStoreReadResult.Resolved
    }

    internal fun set(path: PyStorePath, value: Any?) {
        require(!path.isRoot) {
            "The root of PyStore '$name' cannot be replaced"
        }

        require(value !is PyStoreNode) {
            "PyStoreNode objects cannot be stored as values"
        }

        mutate(path) {
            val parent =
                resolveParent(path, create = true)
                    ?: throw IllegalArgumentException(
                        "Cannot set '$path': parent could not be resolved"
                    )

            writeValue(parent, path.segments.last(), value)
            true
        }
    }

    internal fun getOrPut(path: PyStorePath, initializer: () -> Any?): Any? {
        require(!path.isRoot) { "The root of PyStore '$name' cannot be replaced" }

        var result: Any? = null

        mutate(path) {
            when (val existing = resolve(path.segments)) {
                is PyStoreReadResult.Resolved -> {
                    result = externalizeValue(path, existing.value)
                    false
                }

                PyStoreReadResult.Unresolved -> {
                    val value = initializer()

                    require(value !is PyStoreNode) {
                        "PyStoreNode objects cannot be stored as values"
                    }

                    val parent =
                        resolveParent(path, create = true)
                            ?: throw IllegalArgumentException(
                                "Cannot set '$path': parent could not be resolved"
                            )

                    writeValue(parent, path.segments.last(), value)

                    result = externalizeValue(path, value)
                    true
                }
            }
        }

        return result
    }

    internal fun remove(path: PyStorePath): PyStoreReadResult {
        require(!path.isRoot) { "The root of PyStore '$name' cannot be removed" }

        var result: PyStoreReadResult = PyStoreReadResult.Unresolved

        mutate(path) {
            val parent = resolveParent(path) ?: return@mutate false

            val removed = removeValue(parent, path.segments.last())
            if (removed !is PyStoreReadResult.Resolved) return@mutate false

            result = PyStoreReadResult.Resolved(snapshot(removed.value))
            true
        }

        return result
    }

    internal fun clear(path: PyStorePath) {
        mutate(path) {
            val node =
                resolveNode(path)
                    ?: throw IllegalArgumentException(
                        "Cannot clear '$path': path is not a PyStore node"
                    )

            if (node.isEmpty()) return@mutate false

            node.clear()
            true
        }
    }

    internal fun node(path: PyStorePath): PyStoreNode {
        if (path.isRoot) return this

        mutate(path) {
            var current = root
            var currentPath = PyStorePath.ROOT
            var created = false

            for (segment in path.segments) {
                currentPath = currentPath.child(segment)

                current =
                    if (!current.containsKey(segment)) {
                        NodeData().also {
                            current[segment] = it
                            created = true
                        }
                    } else {
                        current[segment] as? NodeData
                            ?: throw IllegalArgumentException(
                                "Cannot create PyStore node '$path': " +
                                    "'$currentPath' already contains a value"
                            )
                    }
            }

            created
        }

        return ChildNode(this, path)
    }

    internal fun touch(path: PyStorePath) {
        val changes = access {
            changeset.add(path)
            drainChanges()
        }

        changes?.let(::notifyChanges)
    }

    internal fun touchMany(paths: Iterable<PyStorePath>) {
        val changes = access {
            changeset.addAll(paths)
            drainChanges()
        }

        changes?.let(::notifyChanges)
    }

    internal fun expressionValue(path: PyStorePath): PyStoreReadResult = access {
        when (val result = resolve(path.segments)) {
            is PyStoreReadResult.Resolved -> PyStoreReadResult.Resolved(snapshot(result.value))
            PyStoreReadResult.Unresolved -> result
        }
    }

    internal fun children(path: PyStorePath): List<Pair<String, Any?>> = access {
        resolveNode(path)
            ?.map { (key, value) ->
                key to externalizeValue(path.child(key), value)
            }
            .orEmpty()
    }

    internal fun invalidate() {
        synchronized(lock) {
            if (!active) return

            active = false
            root.clear()
            subscribers.clear()
            changeset.clear()
        }
    }

    private fun notifyChanges(changeset: PyStoreChangeset) {
        if (changeset.isEmpty) return

        val listeners =
            synchronized(lock) {
                if (!active || subscribers.isEmpty()) return

                subscribers.toList()
            }

        val pathNames = changeset.paths.joinToString { if (it.isRoot) "<root>" else it.toString() }

        for (listener in listeners) {
            try {
                listener.onChanges(changeset)
            } catch (exception: Exception) {
                logger.warn(
                    "Error notifying subscriber of PyStore '$name' changes at [$pathNames]",
                    exception,
                )
            }
        }
    }

    private fun beginNotificationDeferral() {
        access { notificationDeferralDepth++ }
    }

    private fun endNotificationDeferral() {
        val changes =
            synchronized(lock) {
                check(notificationDeferralDepth > 0) {
                    "No PyStore notification deferral is active"
                }

                notificationDeferralDepth--

                if (!active) {
                    changeset.clear()
                    return@synchronized null
                }

                drainChanges()
            }

        changes?.let(::notifyChanges)
    }

    private fun recordRootChange() {
        val changes =
            synchronized(lock) {
                if (!active) return
                changeset.add(PyStorePath.ROOT)
                drainChanges()
            }

        changes?.let(::notifyChanges)
    }

    private fun resolve(segments: List<String>, create: Boolean = false): PyStoreReadResult {
        var current: Any? = root

        for (segment in segments) {
            if (current == null) {
                return PyStoreReadResult.Unresolved
            }

            val result =
                when (val value = current) {
                    is NodeData -> {
                        when {
                            value.containsKey(segment) -> PyStoreReadResult.Resolved(value[segment])

                            create -> {
                                val child = NodeData()
                                value[segment] = child
                                PyStoreReadResult.Resolved(child)
                            }

                            else -> PyStoreReadResult.Unresolved
                        }
                    }

                    else -> valueResolver.read(value, segment)
                }

            when (result) {
                is PyStoreReadResult.Resolved -> current = result.value

                PyStoreReadResult.Unresolved -> return result
            }
        }

        return PyStoreReadResult.Resolved(current)
    }

    private fun resolveParent(path: PyStorePath, create: Boolean = false): Any? =
        (resolve(path.segments.dropLast(1), create) as? PyStoreReadResult.Resolved)?.value

    private fun writeValue(
        parent: Any,
        key: String,
        value: Any?,
    ) {
        if (parent is NodeData) {
            parent[key] = value
        } else {
            valueResolver.write(parent, key, value)
        }
    }

    private fun removeValue(parent: Any, key: String): PyStoreReadResult =
        if (parent is NodeData) {
            if (parent.containsKey(key)) {
                PyStoreReadResult.Resolved(parent.remove(key))
            } else {
                PyStoreReadResult.Unresolved
            }
        } else {
            valueResolver.remove(parent, key)
        }

    private fun externalize(path: PyStorePath, result: PyStoreReadResult): PyStoreReadResult =
        when (result) {
            is PyStoreReadResult.Resolved ->
                PyStoreReadResult.Resolved(externalizeValue(path, result.value))

            PyStoreReadResult.Unresolved -> result
        }

    private fun externalizeValue(path: PyStorePath, value: Any?): Any? =
        when {
            value !is NodeData -> value

            path.isRoot -> this

            else -> ChildNode(this, path)
        }

    private fun snapshot(value: Any?): Any? =
        when (value) {
            is NodeData -> value.mapValues { snapshot(it.value) }
            else -> value
        }

    private fun checkActive() {
        check(active) {
            "PyStore '$name' is no longer active because its Perspective scope is not running"
        }
    }

    private fun drainChanges(): PyStoreChangeset? {
        if (initializing || notificationDeferralDepth > 0) {
            return null
        }

        return changeset.drain()
    }

    private inline fun <T> access(block: () -> T): T =
        synchronized(lock) {
            checkActive()
            block()
        }

    private inline fun mutate(
        path: PyStorePath,
        block: () -> Boolean,
    ) {
        val changes = access {
            if (!block()) return@access null

            changeset.add(path)
            drainChanges()
        }

        changes?.let(::notifyChanges)
    }

    private fun resolveNode(path: PyStorePath): NodeData? =
        (resolve(path.segments) as? PyStoreReadResult.Resolved)?.value as? NodeData

    fun interface ChangeListener {
        fun onChanges(changeset: PyStoreChangeset)
    }

    private inner class NotificationDeferral : PyObject(), ContextManager {

        override fun __enter__(ts: ThreadState): PyObject {
            beginNotificationDeferral()
            return this@PyStore
        }

        override fun __exit__(
            ts: ThreadState,
            exception: PyException?,
        ): Boolean {
            endNotificationDeferral()
            return false
        }
    }

    private class NodeData : LinkedHashMap<String, Any?>()

    private class ChildNode(
        override val owner: PyStore,
        override val path: PyStorePath,
    ) : PyStoreNode()
}
