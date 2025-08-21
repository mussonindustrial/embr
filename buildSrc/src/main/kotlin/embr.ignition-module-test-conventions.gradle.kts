plugins {
    id("io.kotest")
    id("embr.kotlin-library-conventions")
//    id("embr.e2e-test-environment")
}

tasks.withType<Test>().configureEach {
//    dependsOn(tasks.spotlessCheck)
//    dependsOn(project.parent!!.tasks.assemble)
    useJUnitPlatform()
//    systemProperty("buildDirectory", project.parent!!.layout.buildDirectory.get().asFile.absolutePath)
}

tasks.register<Test>("updateScreenshots") {
    group = "verification"
    description = "Updates Playwright baseline screenshots."

    systemProperty("updateScreenshots", "true")
    outputs.upToDateWhen { false }
}