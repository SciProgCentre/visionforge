plugins {
    id("ru.mipt.npm.gradle.project")

    //Override kotlin version
//    val kotlinVersion = "1.5.20-RC"
//    kotlin("multiplatform") version(kotlinVersion) apply false
//    kotlin("jvm") version(kotlinVersion) apply false
//    kotlin("js") version(kotlinVersion) apply false
}

val dataforgeVersion by extra("0.4.3")
val fxVersion by extra("11")

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://repo.kotlin.link")
        maven("https://maven.jzy3d.org/releases")
    }

    group = "space.kscience"
    version = "0.2.0-dev-22"
}

subprojects {
    if (name.startsWith("visionforge")) {
        plugins.apply("maven-publish")
    }
}

ksciencePublish{
    github("visionforge")
    space()
    sonatype()
}

apiValidation {
    validationDisabled = true
    ignoredPackages.add("info.laht.threekt")
}