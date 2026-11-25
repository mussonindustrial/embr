package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentDefinition

data class ComponentInfo(
    val attributes: ComponentAttributes,
    val definition: ComponentDefinition,
    val diagnostics: List<String>,
) {
    val id: String
        get() = attributes.id

    val path: String
        get() = definition.path

    val name: String
        get() = attributes.name.ifBlank { path.substringAfterLast('/') }
}
