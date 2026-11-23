package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler

data class ComponentAttributes(
    val id: String = "",
    val name: String = "",
    val palettePath: String = "",
    val hidden: Boolean = false,
) {
    companion object {
        const val DATA_KEY = "component"
        val EMPTY = ComponentAttributes()
        private val gson = Compiler.gson
        private val VALID_ID = Regex("""^[A-Za-z0-9][A-Za-z0-9.-]*$""")

        fun fromResource(resource: Resource): ComponentAttributes {
            val json = resource.getAttribute(DATA_KEY).orElse(null) ?: return EMPTY
            return gson.fromJson<ComponentAttributes>(json)
        }

        fun isValidId(id: String): Boolean {
            return VALID_ID.matches(id)
        }
    }

    val hasValidId: Boolean
        get() = isValidId(id)

    val isInPalette: Boolean
        get() = !hidden && paletteFolders.isNotEmpty()

    val paletteFolders: List<String>
        get() = palettePath.split('/').map { it.trim() }.filter { it.isNotEmpty() }

    fun accept(builder: ResourceBuilder) {
        builder.putAttribute(DATA_KEY, gson.toJsonTree(this))
    }
}
