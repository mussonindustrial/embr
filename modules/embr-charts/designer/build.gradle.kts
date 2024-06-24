plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.designer)
    modlImplementation(projects.lib.embrCoreDesigner)

    compileOnly(libs.bundles.perspectiveDesigner)
    modlImplementation(projects.lib.embrPerspectiveDesigner)

    compileOnly(projects.lib.embrCoreCommon)
    compileOnly(projects.modules.embrCharts.common)
}