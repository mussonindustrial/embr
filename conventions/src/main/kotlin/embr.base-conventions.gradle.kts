import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal() // tvOS builds need to be able to fetch a kotlin gradle plugin
}
