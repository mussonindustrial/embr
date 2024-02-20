plugins {
    id("embr.base-conventions")
    id("io.ia.sdk.modl")
    id("pl.allegro.tech.build.axion-release")
    `maven-publish`
}

version = scmVersion.version

allprojects {
    project.version = rootProject.version
}

scmVersion {
    tag {
        prefix.set(project.name)
    }
}