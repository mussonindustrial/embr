plugins {
    id("embr.ignition-module-test-conventions")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.playwright)
    testImplementation(libs.bundles.testcontainers)
}