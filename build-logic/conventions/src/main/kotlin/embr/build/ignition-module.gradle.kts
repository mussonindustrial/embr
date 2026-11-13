package embr.build

import java.text.SimpleDateFormat
import java.util.Date
import libs

plugins {
    id("embr.build.kotlin-library")
    alias(libs.plugins.ignition.module)
}

fun buildTime(): String {
    return SimpleDateFormat("yyyyMMddHH").format(Date())
}

val snapshot = providers.gradleProperty("snapshot").orNull
val snapshotQualifier = if (snapshot == null) "" else "-SNAPSHOT"

ignitionModule { moduleVersion.set("${version}.${buildTime()}$snapshotQualifier") }

afterEvaluate {
    if (snapshot != null) {
        val fileName = ignitionModule.fileName.get().removeSuffix(".modl")
        ignitionModule.fileName.set("$fileName-SNAPSHOT-$snapshot.modl")
    }
}

tasks.deployModl { hostGateway = "http://localhost:8088" }

tasks.writeModuleXml { foldJars = true }
