package embr.build

plugins {
    id("embr.build.kotlin-library")
    `java-library`
}

tasks.jar { archiveBaseName.set("embr-${project.parent?.name}-${project.name}") }

version = project.parent?.version ?: "0.0.0-SNAPSHOT"
