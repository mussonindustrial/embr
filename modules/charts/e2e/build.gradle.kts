import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentTask

plugins {
    id("embr.ignition-module-test-conventions")
    id("embr.e2e-test-environment")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.imagecomparison)
    testImplementation(libs.playwright)
    testImplementation(projects.libraries.testing.e2eTesting)
}

val e2eTests by tasks.registering(TestEnvironmentTask::class) {
    useJUnitPlatform()
    systemProperty("kotest.framework.parallelism", "4")
}