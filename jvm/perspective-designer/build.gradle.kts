plugins {
    id("embr.kotlin-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    compileOnly(libs.bundles.perspectiveDesigner)
}
