import java.util.Date
import java.text.SimpleDateFormat

plugins {
    id("embr.ignition-module-conventions")
}

fun buildTime(): String {
    return SimpleDateFormat("yyyyMMddHH").format(Date())
}

allprojects {
    version = "${project.version}.${buildTime()}"
}

ignitionModule {
    name.set("Perspective Component Example")
    fileName.set("PerspectiveComponentExample.modl")
    id.set("com.mussonindustrial.ignition.perspective.example")
    moduleVersion.set("${project.version}")

    moduleDescription.set("Example Perspective Module.")
    requiredIgnitionVersion.set(libs.versions.ignition)

    projectScopes.putAll(
        mapOf(
            ":common" to "GD",
            ":gateway" to "G",
            ":designer" to "D",
        ),
    )

    moduleDependencies.set(
        mapOf(
            "com.inductiveautomation.perspective" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.perspective.example.GatewayHook" to "G",
            "com.mussonindustrial.ignition.perspective.example.DesignerHook" to "D",
        ),
    )
}

//tasks.register<Copy>("collectResources") {
//    from(layout.buildDirectory.dir("../../../web/packages/example-perspective-component/dist"))
//    into(layout.buildDirectory.dir("../gateway/src/main/resources/dist"))
//}
//
//tasks.register("cleanResources") {
//    delete(layout.buildDirectory.dir("../gateway/src/main/resources/dist"))
//}
//
//tasks.named("build") {
//    dependsOn("collectResources")
//}
//
//tasks.named("clean") {
//    dependsOn("cleanResources")
//}