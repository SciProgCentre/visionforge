plugins {
    id("ru.mipt.npm.gradle.project")
//    kotlin("multiplatform") version "1.5.30-RC" apply false
//    kotlin("js") version "1.5.30-RC" apply false
}

val dataforgeVersion by extra("0.5.1")
val fxVersion by extra("11")

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.kotlin.link")
        maven("https://maven.jzy3d.org/releases")
    }

    group = "space.kscience"
    version = "0.2.0-dev-23"
}

subprojects {
    if (name.startsWith("visionforge")) {
        plugins.apply("maven-publish")
    }
}

ksciencePublish {
    github("visionforge")
    space()
    sonatype()
}

apiValidation {
    validationDisabled = true
    ignoredPackages.add("info.laht.threekt")
}

rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackDevServer.version = "4.0.0-rc.0"
}
