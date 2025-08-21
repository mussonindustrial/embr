plugins {
    id("embr.ignition-module-test-conventions")
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.imagecomparison)
    testImplementation(libs.playwright)
}

val collectModule by tasks.registering(Copy::class) {
    group = "ignition module"

    val moduleProject = parent!!

    val buildModuleTask = moduleProject.tasks.named("build")
    val signModuleTask = moduleProject.tasks.named("signModule")
    val signedModules = signModuleTask.map {
        it.outputs.files.singleFile
    }

    inputs.files(signedModules)
    dependsOn(buildModuleTask, signModuleTask)

    from(signedModules)
    destinationDir = file("build/test-resources")
    rename { "module.modl" }
}

tasks.withType<Test> {
    dependsOn(collectModule)
}

tasks.register<Test>("updateScreenshots") {
    group = "verification"
    description = "Updates Playwright baseline screenshots."

    dependsOn(collectModule)

    useJUnitPlatform()
    systemProperty("updateScreenshots", "true")
    outputs.upToDateWhen { false }
}