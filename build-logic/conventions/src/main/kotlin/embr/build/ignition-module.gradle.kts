package embr.build

import java.text.SimpleDateFormat
import java.util.Date
import libs

plugins {
    id("embr.build.kotlin-library")
    alias(libs.plugins.ignition.module)
}

allprojects { group = "com.mussonindustrial.embr" }

fun buildTime(): String {
    return SimpleDateFormat("yyyyMMddHH").format(Date())
}

ignitionModule { moduleVersion.set("${version}.${buildTime()}") }

tasks.deployModl { hostGateway = "http://localhost:8088" }

tasks.writeModuleXml { foldJars = true }
