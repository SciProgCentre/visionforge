val dataforgeVersion by extra("0.1.3")

plugins {
    val kotlinVersion = "1.3.50"
    val toolsVersion = "0.2.1"

    kotlin("jvm") version kotlinVersion apply false
    id("kotlin-dce-js") version kotlinVersion apply false
    id("scientifik.mpp") version toolsVersion apply false
    id("scientifik.jvm") version toolsVersion apply false
    id("scientifik.js") version toolsVersion apply false
    id("scientifik.publish") version toolsVersion apply false
    id("org.openjfx.javafxplugin") version "0.0.8" apply false
}

allprojects {
    repositories {
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
    }

    group = "hep.dataforge"
    version = "0.1.0-dev"
}

val githubProject by extra("dataforge-vis")
val bintrayRepo by extra("dataforge")

subprojects {
    apply(plugin = "scientifik.publish")
}