plugins {
    id("ru.mipt.npm.gradle.project")
    kotlin("jvm") apply false
    kotlin("jupyter.api") apply false
}

val dataforgeVersion by extra("0.4.0-dev-2")
val kotlinWrappersVersion by extra("pre.148-kotlin-1.4.30")
val fxVersion by extra("14")

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven("https://repo.kotlin.link")
        maven("https://kotlin.bintray.com/kotlin-js-wrappers")
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
    }

    group = "space.kscience"
    version = "0.2.0-dev-8"
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