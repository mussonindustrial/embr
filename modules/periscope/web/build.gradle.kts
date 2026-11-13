plugins {
    id("embr.build.ignition-webjar")
}

group = "com.mussonindustrial.embr.periscope"

tasks.nxBuild { dependsOn(":modules:periscope:gateway:generatePerspectiveGlobals") }
