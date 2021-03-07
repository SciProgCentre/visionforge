pluginManagement {
    val kotlinVersion = "1.4.31"
    val toolsVersion = "0.9.1"

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
        id("ru.mipt.npm.gradle.publish") version toolsVersion
        kotlin("jvm") version kotlinVersion
        kotlin("jupyter.api") version "0.8.3.236"
        kotlin("js") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
    }
}

//enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "visionforge"


include(
//    ":ui",
    ":ui:react",
//    ":ui:ring",
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
    ":jupyter:visionforge-gdml-jupyter"
)