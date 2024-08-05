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


val releaseFiles: Configuration = configurations.create("releaseFiles") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

// I don't understand why this has to be done way.
// I'd like to use the output file of the signModule task, but any way I try gradle throws:
// > Querying the mapped value of task ':modules:embr-charts:signModule' property 'unsigned'
// before task ':modules:embr-charts:zipModule' has completed is not supported
val fileName = ignitionModule.fileName.flatMap { project.provider { it } }
val signedModule = project.provider { file("${projectDir}/build/${fileName.get()}") }

afterEvaluate {
    artifacts {
        add(releaseFiles.name, signedModule) {
            builtBy(tasks.signModule)
        }
    }
}
