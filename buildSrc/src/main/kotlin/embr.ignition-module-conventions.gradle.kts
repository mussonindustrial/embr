import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("embr.kotlin-library-conventions")
    id("io.ia.sdk.modl")
    id("com.github.breadmoirai.github-release")
}

allprojects {
    group = "com.mussonindustrial.embr"

    // Give a friendly error when building project and git tags aren't available.
    if (version == "0.1.0-SNAPSHOT") {
        throw IllegalStateException("Version is not set, please run 'git fetch --tags' command to fetch tags from main repository.")
    }
}

subprojects {
    version = project.parent?.version!!
}

fun buildTime(): String { return SimpleDateFormat("yyyyMMddHH").format(Date()) }
ignitionModule {
    moduleVersion.set(if (version.toString().contains("-SNAPSHOT")) {
        version.toString().replace("-SNAPSHOT", ".${buildTime()}-SNAPSHOT")
    } else {
        "${version}.${buildTime()}"
    })
}

afterEvaluate {
    githubRelease {
        token(project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN"))
        owner("mussonindustrial")
        repo("embr")
        targetCommitish("main")
        tagName("${project.name}-${version}")
        releaseName("${project.name}-${version}")
        generateReleaseNotes(true)
        releaseAssets.from(tasks.signModule.get().signed)
        overwrite.set(true)
    }
}

tasks.githubRelease {
    dependsOn(tasks.build)
}

tasks.deployModl {
    hostGateway = "http://localhost:8088"
}