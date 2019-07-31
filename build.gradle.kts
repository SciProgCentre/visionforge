val dataforgeVersion by extra("0.1.3-dev-10")

plugins{
    val kotlinVersion = "1.3.50-eap-5"

    kotlin("jvm") version kotlinVersion apply false
    id("kotlin2js") version kotlinVersion apply false
    id("kotlin-dce-js") version kotlinVersion apply false
    id("org.jetbrains.kotlin.frontend") version "0.0.45" apply false
    id("scientifik.mpp") version "0.1.4-dev" apply false
    id("scientifik.jvm") version "0.1.4-dev" apply false
    id("scientifik.js") version "0.1.4-dev" apply false
    id("scientifik.publish") version "0.1.4-dev" apply false
    id("org.openjfx.javafxplugin") version "0.0.7" apply false
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("http://npm.mipt.ru:8081/artifactory/gradle-dev-local")
        maven("https://kotlin.bintray.com/js-externals")
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    group = "hep.dataforge"
    version = "0.1.0-dev"
}

val githubProject by extra("dataforge-vis")
val bintrayRepo by extra("dataforge")

subprojects {
    apply(plugin = "scientifik.publish")
}