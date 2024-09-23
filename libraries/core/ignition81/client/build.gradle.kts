plugins {
    id("embr.core-conventions")
}

val platform = projects.libraries.core.ignition81

dependencies {
    compileOnly(libs.ignition.sdk81.common)
    compileOnly(libs.ignition.sdk81.client)
    compileOnly(libs.ignition.sdk81.gson)

    compileOnly(platform.common)
}
