plugins {
    id("embr.ignition-module-test-conventions")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.imagecomparison)
    testImplementation(libs.playwright)
}