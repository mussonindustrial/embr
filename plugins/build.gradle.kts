plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.testcontainers.ignition)
}

gradlePlugin {
    plugins {
        create("ignition-gateway-plugin") {
            id = "com.mussonindustrial.gradle.ignition-gateway-plugin"
            implementationClass = "com.mussonindustrial.gradle.ignition.gateway.IgnitionGatewayPlugin"
        }

    }
}