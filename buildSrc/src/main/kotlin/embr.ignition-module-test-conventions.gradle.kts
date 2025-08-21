import gradle.kotlin.dsl.accessors._6387da9a4856ef4b6bfdab218eed66aa.build
import gradle.kotlin.dsl.accessors._6387da9a4856ef4b6bfdab218eed66aa.spotlessCheck

plugins {
    id("io.kotest")
    id("embr.kotlin-library-conventions")
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
    destinationDir = file("build/test-resources/modules")
}

tasks.withType<Test>().configureEach {
    dependsOn(tasks.spotlessCheck, collectModule)
    useJUnitPlatform()
}

tasks.register<Test>("updateScreenshots") {
    group = "verification"
    description = "Updates Playwright baseline screenshots."

    dependsOn(collectModule)

    systemProperty("updateScreenshots", "true")
    outputs.upToDateWhen { false }
}