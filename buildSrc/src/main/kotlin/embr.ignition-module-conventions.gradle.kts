plugins {
    id("embr.base-conventions")
    id("io.ia.sdk.modl")
    id("pl.allegro.tech.build.axion-release")
    `maven-publish`
    `java-library`
}

version = scmVersion.version

allprojects {
    project.version = version
}

scmVersion {
    checks {
        uncommittedChanges.set(false)
    }
    tag {
        prefix.set(project.name)
        versionSeparator.set("-v")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("module") {
                version = scmVersion.version
                from(components["java"])
                artifact(project.layout.buildDirectory.file(ignitionModule.fileName)) {
                    builtBy(tasks.signModule)
                }
            }
        }
    }
}


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mussonindustrial/embr")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}