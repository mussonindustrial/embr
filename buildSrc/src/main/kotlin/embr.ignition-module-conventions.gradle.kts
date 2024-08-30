import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("embr.kotlin-library-conventions")
    id("io.ia.sdk.modl")
}

allprojects {
    group = "com.mussonindustrial.embr"
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }
ignitionModule {
    moduleVersion.set("${version}.${buildTime()}")
}

tasks.deployModl {
    hostGateway = "http://localhost:8088"
}

tasks.writeModuleXml {
    foldJars = true
}

val releaseFiles: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

afterEvaluate {
    artifacts {
        add(releaseFiles.name, tasks.signModule.flatMap { it.signed })
    }
}
