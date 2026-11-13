plugins {
    id("embr.build.ignition-module-library")
}

group = "com.mussonindustrial.embr.periscope"

dependencies {
    compileOnly(libs.bundles.perspectiveGateway)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.gateway)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.perspective.gateway)
    compileOnly(projects.modules.periscope.common)
    modlImplementation(projects.modules.periscope.web)
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.servlet)
    compileOnly(libs.jetty.servlets)
    modlImplementation(projects.libraries.core.servlets)
}

val perspectiveScripts = layout.buildDirectory.dir("perspective-scripts")

val extractPerspectiveScripts by
    tasks.registering(Sync::class) {
        val perspectiveApp =
            configurations.compileClasspath.map { classpath ->
                classpath.filter { it.name.startsWith("app-") }.map { zipTree(it) }
            }
        from(perspectiveApp) { include("mounted/js/*.js") }
        into(perspectiveScripts)
    }

val generatePerspectiveGlobals by
    tasks.registering(Exec::class) {
        val generator = file("perspective-globals.mjs")
        val perspectiveGlobals = rootProject.file("modules/periscope/web/perspective-globals.json")
        inputs.file(generator)
        inputs.files(rootProject.tasks.named("yarnInstall"))
        inputs.files(extractPerspectiveScripts)
        outputs.file(perspectiveGlobals)
        commandLine(
            "node",
            generator,
            perspectiveScripts.get().dir("mounted/js").asFile,
            perspectiveGlobals,
        )
    }

tasks.processResources {
    from(generatePerspectiveGlobals) {
        into("com/mussonindustrial/ignition/embr/periscope/js/modules")
    }
}
