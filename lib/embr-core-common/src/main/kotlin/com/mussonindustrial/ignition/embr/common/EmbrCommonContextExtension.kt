package com.mussonindustrial.ignition.embr.common

interface EmbrCommonContextExtension {

    fun requireModule(moduleId: String, run: () -> Unit)

}