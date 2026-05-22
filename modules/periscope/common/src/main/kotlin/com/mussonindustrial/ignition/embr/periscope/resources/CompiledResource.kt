package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler
import kotlin.jvm.optionals.getOrNull

sealed interface CompiledResource {

    val fileContents: FileContents
    val fileLocations: FileLocations
    val compilerMetadata: CompilerMetadata

    fun applyToBuilder(builder: ResourceBuilder) {
        fileLocations.accept(builder)
        fileContents.accept(fileLocations, builder)
        compilerMetadata.accept(builder)
    }

    data class FileLocations(val source: String, val compiled: String) {
        companion object {
            const val DATA_KEY = "files"
            val gson = Compiler.gson

            fun fromResource(resource: Resource): FileLocations {
                val json = resource.getAttribute(DATA_KEY).orElse(null)
                if (json == null)
                    throw IllegalArgumentException("Malformed resource, no file locations found.")
                return gson.fromJson<FileLocations>(json)
            }
        }

        fun accept(builder: ResourceBuilder) {
            builder.putAttribute(DATA_KEY, gson.toJsonTree(this))
        }
    }

    data class FileContents(val source: String, val compiled: String) {
        companion object {
            val EMPTY = FileContents("", "")

            fun fromResource(resource: Resource): FileContents {
                val locations = FileLocations.fromResource(resource)
                return FileContents(
                    resource.getData(locations.source).getOrNull()?.bytesAsString ?: "",
                    resource.getData(locations.compiled).getOrNull()?.bytesAsString ?: "",
                )
            }
        }

        fun accept(locations: FileLocations, builder: ResourceBuilder) {
            builder.putData(locations.source, source.encodeToByteArray())
            builder.putData(locations.compiled, compiled.encodeToByteArray())
        }
    }

    data class CompilerMetadata(val version: String) {
        companion object {
            const val DATA_KEY = "compiler"
            val gson = Compiler.gson
            val EMPTY = CompilerMetadata("")

            fun fromResource(resource: Resource): CompilerMetadata =
                gson.fromJson<CompilerMetadata>(resource.getAttribute(DATA_KEY).orElse(null))
        }

        fun accept(builder: ResourceBuilder) {
            builder.putAttribute(DATA_KEY, gson.toJsonTree(this))
        }
    }
}
