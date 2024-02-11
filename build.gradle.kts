plugins {
    alias(libs.plugins.kotlin) apply false
}

allprojects {
    group = "com.mussonindustrial.ignition"

    repositories {
        mavenLocal()
        mavenCentral()

        maven(url = "https://nexus.inductiveautomation.com/repository/public/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-thirdparty/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-releases/")
        maven(url = "https://nexus.inductiveautomation.com/repository/inductiveautomation-snapshots/")

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

//    configurations.all {
//        resolutionStrategy.cacheChangingModulesFor(0, "minutes")
//    }

    }
}

