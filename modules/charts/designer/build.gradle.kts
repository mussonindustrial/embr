import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentTask

plugins {
    id("embr.ignition-module-library-conventions")
    id("embr.e2e-test-environment")
}

group = "com.mussonindustrial.embr.charts"

dependencies {
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.core.common)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.core.designer)
    modlImplementation(projects.libraries.perspective.designer)
    compileOnly(projects.modules.charts.common)
}

val customTest by tasks.registering(TestEnvironmentTask::class)