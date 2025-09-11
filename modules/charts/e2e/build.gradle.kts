import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentTask

plugins {
    id("embr.build.ignition-module-test")
    id("embr.e2e.test-environment")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.imagecomparison)
    testImplementation(libs.playwright)
    testImplementation(projects.libraries.testing.e2eTesting)
}
