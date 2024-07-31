plugins {
    id("embr.base-conventions")
    id("com.github.node-gradle.node")
    id("com.coditory.webjar")
}

repositories {
    ivy {
        name = "Node.js"
        setUrl("https://nodejs.org/dist/")
        patternLayout {
            artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
        }
        metadataSources {
            artifact()
        }
        content {
            includeModule("org.nodejs", "node")
        }
    }
}

node {
    version.set("20.11.0")
    yarnVersion.set("1.22.19")
    npmVersion.set("10.5.0")
    download.set(false)
    workDir = file("${rootProject.projectDir}/.gradle/nodejs")
    npmWorkDir = file("${rootProject.projectDir}/.gradle/npm")
    yarnWorkDir = file("${rootProject.projectDir}/.gradle/yarn")
    distBaseUrl.set("https://nodejs.org/dist")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.create<Delete>("cleanDist") {
    group = "node"
    delete = setOf("${projectDir}/dist")
}

tasks.clean {
    dependsOn("cleanDist")
}