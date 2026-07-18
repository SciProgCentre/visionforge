rootProject.name = "visionforge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {

    val toolsVersion: String = providers.gradleProperty("toolsVersion").get()

    repositories {
        mavenLocal()
        maven("https://repo.kotlin.link")
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        id("space.kscience.gradle.project") version toolsVersion
        id("space.kscience.gradle.mpp") version toolsVersion
        id("space.kscience.gradle.jvm") version toolsVersion
    }
}

dependencyResolutionManagement {

    val toolsVersion: String = providers.gradleProperty("toolsVersion").get()

    repositories {
        mavenLocal()
        maven("https://repo.kotlin.link")
        mavenCentral()
    }

    versionCatalogs {
        create("spclibs") {
            from("space.kscience:version-catalog:$toolsVersion")
        }
    }
}

include(
    ":visionforge-core",
    ":visionforge-compose-html",
    ":visionforge-compose-multiplatform",
    ":visionforge-solid",
    ":visionforge-threejs",
    ":visionforge-threejs:visionforge-threejs-server",
    ":visionforge-gdml",
    ":cern-root-loader",
    ":visionforge-server",
//    ":visionforge-plotly",
    ":visionforge-tables",
    ":visionforge-markdown",
    ":demo:solid-showcase",
    ":demo:gdml",
    ":demo:muon-monitor",
    ":demo:sat-demo",
    ":demo:playground",
    ":demo:js-playground",
    ":demo:compose-desktop-demo",
    ":visionforge-jupyter",
    ":visionforge-jupyter:visionforge-jupyter-common",
    ":plotly-kt",
    ":plotly-kt:plotly-kt-core",
    ":plotly-kt:plotly-kt-server",
//    ":plotly-kt:plotly-kt-script",
    ":plotly-kt:examples",
//    ":plotly:examples:fx-demo",
    ":plotly-kt:examples:compose-demo",
    ":plotly-kt:examples:js-demo",
    ":plotly-kt:examples:native-demo",
    ":plotly-kt:examples:wasm-demo"
)
