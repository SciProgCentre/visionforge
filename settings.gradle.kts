pluginManagement {

    val toolsVersion = "0.10.2"

    repositories {
        maven("https://repo.kotlin.link")
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("ru.mipt.npm.gradle.project") version toolsVersion
        id("ru.mipt.npm.gradle.mpp") version toolsVersion
        id("ru.mipt.npm.gradle.jvm") version toolsVersion
        id("ru.mipt.npm.gradle.js") version toolsVersion
    }
}

rootProject.name = "visionforge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
//    ":ui",
    ":ui:react",
    ":ui:ring",
//    ":ui:material",
    ":ui:bootstrap",
    ":visionforge-core",
    ":visionforge-solid",
    ":visionforge-fx",
    ":visionforge-threejs",
    ":visionforge-threejs:visionforge-threejs-server",
    ":visionforge-gdml",
    ":cern-root-loader",
    ":visionforge-server",
    ":visionforge-plotly",
    ":visionforge-markdown",
    ":demo:solid-showcase",
    ":demo:gdml",
    ":demo:muon-monitor",
    ":demo:sat-demo",
    ":demo:playground",
    ":demo:jupyter-playground",
    ":demo:plotly-fx",
    ":demo:js-playground",
    ":jupyter:visionforge-gdml-jupyter"
)
