plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    implementation(libs.bundles.kotest)
    implementation(libs.bundles.testcontainers)
    implementation(libs.imagecomparison)
    implementation(libs.playwright)
}