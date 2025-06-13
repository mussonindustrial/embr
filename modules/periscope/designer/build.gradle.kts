plugins {
    id("embr.ignition-module-library-conventions")
}

dependencies {
    compileOnly(libs.bundles.perspectiveDesigner)
    compileOnly(projects.libraries.core.common)
    modlImplementation(projects.libraries.core.designer)
    compileOnly(projects.libraries.perspective.common)
    modlImplementation(projects.libraries.perspective.designer)

    compileOnly(projects.modules.periscope.common)
    modlImplementation(projects.libraries.javascript.monacoEditor)
}