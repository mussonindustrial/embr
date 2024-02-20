plugins {
    id("embr.base-conventions")
    id("io.ia.sdk.modl")
    id("pl.allegro.tech.build.axion-release")
    id("com.github.breadmoirai.github-release")
    `maven-publish`
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
                artifactId = project.name
                setArtifacts(listOf(
                    artifact(project.layout.buildDirectory.file(ignitionModule.fileName)) {
                        builtBy(tasks.signModule)
                    })
                )
            }
        }
    }
    githubRelease {
        token(project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN"))
        owner("mussonindustrial")
        repo("embr")
        tagName("${project.name}-${version}")
        targetCommitish("main")
        releaseName("${ignitionModule.fileName.get()} (${version})")
        generateReleaseNotes(true)
        releaseAssets.from(tasks.signModule.get().signed)
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

tasks.githubRelease {
    dependsOn(tasks.build)
}
