package com.mussonindustrial.ignition.embr.periscope.pystore

import com.mussonindustrial.ignition.embr.periscope.utils.toPyValue
import java.util.concurrent.atomic.AtomicBoolean
import org.python.core.PyObject

class PyStoreListener
internal constructor(
    private val store: PyStore,
    val path: PyStorePath,
    private val callback: PyObject,
) : PyObject(), AutoCloseable, PyStore.ChangeListener {

    private val closed = AtomicBoolean()

    override fun onChanges(changeset: PyStoreChangeset) {
        if (closed.get() || !changeset.affects(path)) {
            return
        }

        val event = PyStoreChangeEvent(store, path, changeset)
        callback.__call__(event.toPyValue())
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            store.unsubscribe(this)
        }
    }

    class PyStoreChangeEvent(
        val store: PyStore,
        internal val listenPath: PyStorePath,
        internal val changeset: PyStoreChangeset,
    ) {

        val path: String
            get() = listenPath.toString()

        val changedPaths: List<String>
            get() = changeset.paths.map { it.toString() }

        val matchingPaths: List<String>
            get() = changeset.paths.filter { listenPath.isRelatedTo(it) }.map { it.toString() }

        fun affects(relativePath: String): Boolean =
            changeset.affects(listenPath.resolve(PyStorePath.parse(relativePath)))
    }
}
