repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}

tasks.register("buildAll") {
    group = "build"
    dependsOn(gradle.includedBuilds.mapNotNull { it.task(":build") })
}

tasks.register("cleanAll") {
    group = "build"
    dependsOn(gradle.includedBuilds.mapNotNull { it.task(":clean") })
}