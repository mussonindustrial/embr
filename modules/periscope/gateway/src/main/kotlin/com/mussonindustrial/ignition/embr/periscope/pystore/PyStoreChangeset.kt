package com.mussonindustrial.ignition.embr.periscope.pystore

class PyStoreChangeset internal constructor() {

    private val changedPaths = mutableListOf<PyStorePath>()

    val paths: List<PyStorePath>
        get() = changedPaths.toList()

    val isEmpty: Boolean
        get() = changedPaths.isEmpty()

    val size: Int
        get() = changedPaths.size

    fun affects(path: PyStorePath): Boolean = changedPaths.any {
        path.isRelatedTo(it)
    }

    internal fun add(path: PyStorePath) {
        if (changedPaths.any { it.isAncestorOf(path) }) {
            return
        }

        changedPaths.removeAll { path.isAncestorOf(it) }
        changedPaths += path
    }

    internal fun addAll(paths: Iterable<PyStorePath>) {
        paths.forEach(::add)
    }

    internal fun drain(): PyStoreChangeset? {
        if (isEmpty) return null

        val result = PyStoreChangeset()
        result.changedPaths += changedPaths
        changedPaths.clear()

        return result
    }

    internal fun clear() {
        changedPaths.clear()
    }
}
