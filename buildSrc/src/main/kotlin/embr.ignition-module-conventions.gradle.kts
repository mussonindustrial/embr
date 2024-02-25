import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("embr.kotlin-library-conventions")
    id("io.ia.sdk.modl")
//    id("pl.allegro.tech.build.axion-release")
    id("io.github.kotlin-artisans.changesets")
    id("com.github.breadmoirai.github-release")
    `maven-publish`
}

//version = scmVersion.version

allprojects {
    project.version = version
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }
//scmVersion {
//    checks {
//        uncommittedChanges.set(false)
//    }
//    tag {
//        prefix.set(project.name)
//        versionSeparator.set("-v")
//    }
//    useHighestVersion.set(true)
//    versionIncrementer("incrementPrerelease", mapOf("initialPreReleaseIfNotOnPrerelease" to "RC1"))
//    snapshotCreator { version, _ -> "${version}.${buildTime()}" }
//    repository {
//        type = "git"
//        remote = "origin"
//        customUsername=project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
//        customPassword=project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
//    }
//}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("module") {
//                version = scmVersion.version
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
        targetCommitish("main")
        tagName("${project.name}-${version}")
        releaseName("${project.name}-${version}")
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

tasks.deployModl {
    hostGateway = "http://localhost:8088"
}