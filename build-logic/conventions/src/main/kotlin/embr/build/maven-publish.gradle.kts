package embr.build

import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

plugins { id("maven-publish") }

val groupSuffix: String by lazy {
    val suffix = project.findProperty("embr.groupSuffix")?.toString() ?: ""
    suffix.trim()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            project.afterEvaluate {
                val targetNamespace = "com.mussonindustrial.embr"
                val currentGroup = project.group.toString()

                if (groupSuffix.isNotBlank() && currentGroup.startsWith(targetNamespace)) {
                    groupId =
                        currentGroup.replaceFirst(targetNamespace, "$targetNamespace.$groupSuffix")
                }
            }
            components.matching { it.name == "java" }.all { from(this) }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mussonindustrial/embr")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf("Artifact does not already exist in GitHub Packages") {
        val repoUrl = repository.url.toString()

        if (!repoUrl.contains("maven.pkg.github.com")) return@onlyIf true

        val actor = System.getenv("GITHUB_ACTOR")
        val token = System.getenv("GITHUB_TOKEN")

        require(!actor.isNullOrBlank()) {
            "Publishing aborted: GITHUB_ACTOR environment variable is missing."
        }
        require(!token.isNullOrBlank()) {
            "Publishing aborted: GITHUB_TOKEN environment variable is missing."
        }

        val groupId = publication.groupId
        val artifactId = publication.artifactId
        val version = publication.version

        require(groupId.isNotBlank()) {
            "Publishing aborted: Publication groupId is not configured."
        }
        require(artifactId.isNotBlank()) {
            "Publishing aborted: Publication artifactId is not configured."
        }
        require(version.isNotBlank()) {
            "Publishing aborted: Publication version is not configured."
        }

        val groupPath = groupId.replace(".", "/")
        val pomUrl = "$repoUrl/$groupPath/$artifactId/$version/$artifactId-$version.pom"

        try {
            val connection = URI(pomUrl).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"

            val auth = Base64.getEncoder().encodeToString("$actor:$token".toByteArray())
            connection.setRequestProperty("Authorization", "Basic $auth")

            when (val responseCode = connection.responseCode) {
                200 -> {
                    logger.lifecycle(
                        "Artifact $groupId:$artifactId:$version already exists. Skipping publish."
                    )
                    false
                }

                404 -> true

                else ->
                    throw IllegalStateException(
                        "Publishing aborted: GitHub Packages returned HTTP $responseCode " +
                            "while checking $groupId:$artifactId:$version."
                    )
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Publishing aborted: Failed to verify if artifact exists on GitHub Packages.",
                e,
            )
        }
    }
}
