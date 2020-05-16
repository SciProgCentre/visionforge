pluginManagement {
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

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "scientifik.mpp", "scientifik.publish", "scientifik.jvm", "scientifik.js" -> useModule("scientifik:gradle-tools:${requested.version}")
                "org.openjfx.javafxplugin" -> useModule("org.openjfx:javafx-plugin:${requested.version}")
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
