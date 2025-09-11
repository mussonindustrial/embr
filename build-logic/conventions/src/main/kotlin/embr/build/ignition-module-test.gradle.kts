package embr.build

import libs

plugins {
    alias(libs.plugins.kotest)
    id("embr.build.kotlin-library")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

tasks.register<Test>("updateScreenshots") {
    group = "verification"
    description = "Updates Playwright baseline screenshots."

    systemProperty("updateScreenshots", "true")
    outputs.upToDateWhen { false }
}
