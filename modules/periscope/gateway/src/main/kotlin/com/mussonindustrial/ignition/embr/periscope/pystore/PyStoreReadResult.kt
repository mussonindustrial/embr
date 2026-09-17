package com.mussonindustrial.ignition.embr.periscope.pystore

sealed class PyStoreReadResult {
    data class Resolved(val value: Any?) : PyStoreReadResult()

    data object Unresolved : PyStoreReadResult()
}
