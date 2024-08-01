plugins {
    base
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    gradlePluginPortal()
}


allprojects {
  apply {
      plugin("project-report")
  }
}

tasks.register("projectReportAll") {
    // All project reports of subprojects
    allprojects.forEach {
        dependsOn(it.tasks["projectReport"])
    }

    // All projectReportAll of included builds
    gradle.includedBuilds.forEach {
        dependsOn(it.task(":projectReportAll"))
    }
}