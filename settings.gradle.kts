pluginManagement {
    val kotlinVersion = "1.3.72"
    val toolsVersion = "0.5.2"

    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/mipt-npm/dataforge")
        maven("https://dl.bintray.com/mipt-npm/scientifik")
        maven("https://dl.bintray.com/mipt-npm/dev")
    }

    plugins {
        kotlin("jvm") version kotlinVersion
        id("scientifik.mpp") version toolsVersion
        id("scientifik.jvm") version toolsVersion
        id("scientifik.js") version toolsVersion
        id("scientifik.publish") version toolsVersion
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "scientifik.mpp", "scientifik.publish", "scientifik.jvm", "scientifik.js" -> useModule("scientifik:gradle-tools:${toolsVersion}")
            }
        }
    }
}

//enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "dataforge-vis"

include(
    ":ui",
    ":ui:react",
    ":ui:ring",
    ":ui:material",
    ":ui:bootstrap",
    ":dataforge-vis-common",
    ":dataforge-vis-spatial",
    ":dataforge-vis-spatial-gdml",
    ":demo:spatial-showcase",
    ":demo:gdml",
    ":demo:muon-monitor",
    ":playground"
)
