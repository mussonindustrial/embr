package com.mussonindustrial.ignition.embr.periscope.js.modules

interface VirtualModule {
    val name: String
    val namedExports: List<String>

    fun emit(): String
}
