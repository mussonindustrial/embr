group = "com.mussonindustrial.ignition"

plugins {
    id("embr.base-conventions")
    `java-library`
}

repositories {
    maven(url = "https://nexus.inductiveautomation.com/repository/public/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-thirdparty/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
    maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_11
}

val resourcesPath = "../src/main/resources/web"
val modlWebResource: Configuration by configurations.creating {
    isCanBeConsumed = true
}

tasks {
    named("processResources") {
        dependsOn("syncWebResources")
    }

    register<Sync>("syncWebResources") {
        group = "ignition module"
        val resourceFiles: FileCollection = modlWebResource
        from(resourceFiles)
        into(layout.buildDirectory.dir(resourcesPath))
    }

    named("clean") {
        doFirst { delete(layout.buildDirectory.dir(resourcesPath)) }
    }
}