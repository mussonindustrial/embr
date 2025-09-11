plugins {
    id("embr.build.kotlin-library")
}

dependencies {
    implementation(libs.bundles.kotest)
    implementation(libs.bundles.testcontainers)
    implementation(libs.imagecomparison)
    implementation(libs.playwright)
}