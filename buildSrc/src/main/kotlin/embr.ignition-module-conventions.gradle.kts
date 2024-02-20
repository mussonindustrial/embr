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
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}