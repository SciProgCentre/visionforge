import scientifik.fx
import scientifik.serialization

val dataforgeVersion by extra("0.1.5")

plugins {
    val toolsVersion = "0.4.0"
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
    version = "0.1.2-dev"
}

val githubProject by extra("dataforge-vis")
val bintrayRepo by extra("dataforge")
val fxVersion by extra("14")

subprojects {
    apply(plugin = "scientifik.publish")
    serialization()
    afterEvaluate {
        fx(scientifik.FXModule.CONTROLS, version = fxVersion)
    }
}