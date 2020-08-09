import scientifik.useFx
import scientifik.useSerialization

val dataforgeVersion by extra("0.1.8")

plugins {
    id("scientifik.mpp") apply false
    id("scientifik.jvm") apply false
    id("scientifik.js") apply false
    id("scientifik.publish") apply false
    id("org.jetbrains.changelog") version "0.4.0"
}

allprojects {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
    }

    group = "hep.dataforge"
    version = "0.1.5-dev-2"
}

val githubProject by extra("visionforge")
val bintrayRepo by extra("dataforge")
val fxVersion by extra("14")

subprojects {
    if(name.startsWith("visionforge")) {
        apply(plugin = "scientifik.publish")
    }
    useSerialization()
    useFx(scientifik.FXModule.CONTROLS, version = fxVersion)
}