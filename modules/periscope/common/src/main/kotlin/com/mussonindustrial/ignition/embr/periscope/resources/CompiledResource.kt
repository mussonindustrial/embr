package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler

sealed interface CompiledResource {

    val fileContents: FileContents
    val fileLocations: FileLocations
    val compilerMetadata: CompilerMetadata

    fun applyToBuilder(builder: ProjectResourceBuilder) {
        fileLocations.accept(builder)
        fileContents.accept(fileLocations, builder)
        compilerMetadata.accept(builder)
    }

    data class FileLocations(val source: String, val compiled: String) {
        companion object {
            const val DATA_KEY = "files"
            val gson = Compiler.gson

            fun fromResource(resource: ProjectResource): FileLocations {
                val json = resource.getAttribute(DATA_KEY).orElse(null)
                if (json == null)
                    throw IllegalArgumentException("Malformed resource, no file locations found.")
                return gson.fromJson<FileLocations>(json)
            }
        }

        fun accept(builder: ProjectResourceBuilder) {
            builder.putAttribute(DATA_KEY, gson.toJsonTree(this))
        }
    }

    data class FileContents(val source: String, val compiled: String) {
        companion object {
            val EMPTY = FileContents("", "")

            fun fromResource(resource: ProjectResource): FileContents {
                val locations = FileLocations.fromResource(resource)
                return FileContents(
                    resource.getData(locations.source)?.decodeToString() ?: "",
                    resource.getData(locations.compiled)?.decodeToString() ?: "",
                )
            }
        }

        fun accept(locations: FileLocations, builder: ProjectResourceBuilder) {
            builder.putData(locations.source, source.encodeToByteArray())
            builder.putData(locations.compiled, compiled.encodeToByteArray())
        }
    }

    data class CompilerMetadata(val version: String) {
        companion object {
            const val DATA_KEY = "compiler"
            val gson = Compiler.gson
            val EMPTY = CompilerMetadata("")

            fun fromResource(resource: ProjectResource): CompilerMetadata =
                gson.fromJson<CompilerMetadata>(resource.getAttribute(DATA_KEY).orElse(null))
        }

        fun accept(builder: ProjectResourceBuilder) {
            builder.putAttribute(DATA_KEY, gson.toJsonTree(this))
        }
    }
}
