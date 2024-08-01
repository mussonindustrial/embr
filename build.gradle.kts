plugins {
    base
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}


tasks.register("buildModules") {
    group = "build"
    dependsOn(":modules:embr-charts:build")
    dependsOn(":modules:embr-tag-stream:build")
}