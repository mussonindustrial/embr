package embr.build

plugins {
    id("embr.build.kotlin-library")
    id("embr.build.maven-publish")
    `java-library`
}

tasks.jar { archiveBaseName.set("embr-${project.parent?.name}-${project.name}") }
