plugins {
    id("embr.kotlin-library-conventions")
    `java-library`
}

tasks.jar {
    archiveBaseName.set("${project.parent?.name}-${project.name}")
}
version = project.parent?.version ?: "0.0.0-SNAPSHOT"