import scientifik.useSerialization

val dataforgeVersion by extra("0.1.5-dev-6")

plugins {
    val kotlinVersion = "1.3.61"
    val toolsVersion = "0.3.1"

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
        mavenLocal()
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
        maven("https://kotlin.bintray.com/js-externals")
//        maven("https://dl.bintray.com/gbaldeck/kotlin")
//        maven("https://dl.bintray.com/rjaros/kotlin")
    }

    group = "hep.dataforge"
    version = "0.1.0-dev"
}

subprojects{
    this.useSerialization()
}

val githubProject by extra("dataforge-vis")
val bintrayRepo by extra("dataforge")

subprojects {
    apply(plugin = "scientifik.publish")
}