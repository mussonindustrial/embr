plugins {
    id("embr.ignition-module-library-conventions")
}

val resourcesPath = "../src/main/resources/web"
val jsBundles: Configuration by configurations.creating {
    isCanBeConsumed = false
}

dependencies {
    compileOnly(libs.bundles.gateway)
    compileOnly(libs.bundles.perspectiveFateway)
    compileOnly(projects.common)
    compileOnly(projects.web)
    jsBundles(project(path = ":web", configuration = "example_perspective_component"))
}

tasks {
    named("processResources") {
        dependsOn("collectWebResources")
    }
    register<Copy>("collectWebResources") {
        group = "build"
        val jsBundleFiles: FileCollection = jsBundles
        from(jsBundleFiles)
        into(layout.buildDirectory.dir(resourcesPath))
    }

    named("clean") {
        doFirst { delete(layout.buildDirectory.dir(resourcesPath)) }
    }
}