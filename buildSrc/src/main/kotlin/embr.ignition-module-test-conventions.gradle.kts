import gradle.kotlin.dsl.accessors._6387da9a4856ef4b6bfdab218eed66aa.build
import gradle.kotlin.dsl.accessors._6387da9a4856ef4b6bfdab218eed66aa.spotlessCheck

plugins {
    id("io.kotest")
    id("embr.kotlin-library-conventions")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    dependsOn(tasks.spotlessCheck)
}