plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        ktfmt().kotlinlangStyle()
    }
}

tasks.compileKotlin { dependsOn(tasks.spotlessCheck) }

dependencies {
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.ignition)
}
