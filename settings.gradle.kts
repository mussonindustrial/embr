rootProject.name = "embr"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val architectureElements = mutableListOf<ArchitectureElementBuilder>()

/** Core Library */
val core = library("core") {
    target("ignition81") {
        subproject("client")
        subproject("common")
        subproject("designer")
        subproject("gateway")
        subproject("perspective-common")
        subproject("perspective-designer")
        subproject("servlets")
    }
}

/** Charts Module */
val chartsModule = module("charts") {
    target("core") {
        subproject("web")
    }

    target("ignition81") {
        subproject("common")
        subproject("designer")
        subproject("gateway")
    }
}
//
///**
// * SSE Module
// */
//val eventStreamModule = module("event-stream") {
//    library("shared") {
//        subproject("web")
//    }
//    target("sdk-8.1") {
//        subproject("client")
//    }
//    target("sdk-8.3") {
//        subproject("client")
//    }
//}

/** Thermodynamics Module */
val thermoModule = module("thermo") {
    target("ignition81") {
        subproject("common")
        subproject("client")
        subproject("designer")
        subproject("gateway")
    }
}

/** Defines a module */
fun module(moduleName: String, moduleConfiguration: ModuleBuilder.() -> Unit): Module {
    val module = ModuleBuilder(moduleName)
    architectureElements.add(module)
    module.moduleConfiguration()
    return module.build()
}

/** Defines a library */
fun library(libraryName: String, libraryConfiguration: LibraryBuilder.() -> Unit): Library {
    val library = LibraryBuilder(libraryName)
    architectureElements.add(library)
    library.libraryConfiguration()
    return library.build()
}

class ElementId(private val id: String) {
    override fun toString(): String {
        return id
    }
}

sealed class ArchitectureElement(
    val name: String,
    val id: ElementId
)

class Target(name: String, id: ElementId) : ArchitectureElement(name, id)
class Library(name: String, id: ElementId, val targets: List<Target>) : ArchitectureElement(name, id)
class Module(name: String, id: ElementId, val targets: List<Target>) : ArchitectureElement(name, id)

sealed class ArchitectureElementBuilder(
    val name: String
) {
    val id: ElementId = ElementId(name.replace("-", "_"))

    abstract fun build(): ArchitectureElement
}

class ProjectScope(
    private val baseFilePath: String,
    private val baseProjectPath: String,
) {
    constructor(baseFilePath: String): this(baseFilePath, "")

    fun subproject(projectName: String): ProjectScope {
        val projectPath = "$baseProjectPath:$projectName"
        val projectFilePath = "$baseFilePath/$projectName"

        include(projectPath)
        project(projectPath).projectDir = file(projectFilePath)
        return ProjectScope(projectFilePath, projectPath)
    }

    fun subproject(projectName: String, projectConfiguration: ProjectScope.() -> Unit) {
        subproject(projectName).projectConfiguration()
    }
}

abstract class SubprojectContainer(
    name: String,
    private val projectScope: ProjectScope
): ArchitectureElementBuilder(name){

    fun subproject(projectName: String): ProjectScope {
        return projectScope.subproject(projectName)
    }

    fun subproject(projectName: String, projectConfiguration: ProjectScope.() -> Unit) {
        subproject(projectName).projectConfiguration()
    }

}

class TargetBuilder(
    name: String,
    projectScope: ProjectScope
) : SubprojectContainer(name, projectScope) {

    override fun build(): Target {
        return Target(name, id)
    }
}

class LibraryBuilder(
    name: String,
    projectScope: ProjectScope
) : SubprojectContainer(name, projectScope) {
    private val targets = mutableListOf<TargetBuilder>()
    constructor(name: String) : this(name, ProjectScope("libraries/$name", ":$name"))

    fun target(targetName: String, targetConfiguration: TargetBuilder.() -> Unit) {
        val target = TargetBuilder(targetName, subproject(targetName))
        targets.add(target)
        target.targetConfiguration()
    }

    override fun build(): Library {
        return Library(name, id, targets.map { it.build() })
    }
}

class ModuleBuilder(
    name: String,
    projectScope: ProjectScope
) : SubprojectContainer(name, projectScope) {
    private val targets = mutableListOf<TargetBuilder>()
    constructor(name: String) : this(name, ProjectScope("modules/$name", ":$name"))

    fun target(targetName: String, targetConfiguration: TargetBuilder.() -> Unit) {
        val target = TargetBuilder(targetName, subproject(targetName))
        targets.add(target)
        target.targetConfiguration()
    }

    override fun build(): Module {
        return Module(name, id, targets.map { it.build() })
    }
}