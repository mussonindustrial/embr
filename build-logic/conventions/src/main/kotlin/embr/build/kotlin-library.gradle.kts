package embr.build

import libs

plugins {
    id("embr.build.base")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
}

kotlin { jvmToolchain(libs.versions.java.get().toInt()) }

spotless { kotlin { ktfmt().kotlinlangStyle() } }

tasks.build { dependsOn(tasks.spotlessCheck) }
