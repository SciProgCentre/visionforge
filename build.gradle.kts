plugins {
    id("ru.mipt.npm.gradle.project")
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("js") apply false
    kotlin("jupyter.api") apply false
    id("ru.mipt.npm.gradle.js") apply false
}

val dataforgeVersion by extra("0.4.1")
val kotlinWrappersVersion by extra("pre.152-kotlin-1.4.32")
val fxVersion by extra("11")

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://repo.kotlin.link")
        maven("https://maven.jzy3d.org/releases")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }

    group = "space.kscience"
    version = "0.2.0-dev-17"
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