plugins {
    id("embr.kotlin-library-conventions")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}


tasks.jar {
    archiveBaseName.set("embr-${project.parent?.name}-${project.name}")
}
version = project.parent?.version ?: "0.0.0-SNAPSHOT"