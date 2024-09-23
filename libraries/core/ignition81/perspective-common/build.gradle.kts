plugins {
    id("embr.core-conventions")
}

dependencies {
    compileOnly(libs.ignition.sdk81.common)
    compileOnly(libs.ignition.sdk81.perspective.common)
    compileOnly(libs.ignition.sdk81.gson)
}
