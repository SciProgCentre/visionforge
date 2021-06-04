pluginManagement {
    val kotlinVersion = "1.5.10"
    val toolsVersion = "0.9.10"

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
        kotlin("multiplatform") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("jupyter.api") version "0.9.1-20"
    }
}

//enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "visionforge"


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
    ":visionforge-server",
    ":visionforge-plotly",
    ":demo:solid-showcase",
    ":demo:gdml",
    ":demo:muon-monitor",
    ":demo:sat-demo",
    ":demo:playground",
    ":demo:jupyter-playground",
    ":demo:plotly-fx",
    ":jupyter:visionforge-gdml-jupyter"
)