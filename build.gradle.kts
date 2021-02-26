plugins {
    id("ru.mipt.npm.gradle.project")
    kotlin("jvm") apply false
    kotlin("jupyter.api") apply false
}

val dataforgeVersion by extra("0.3.0")
val ktorVersion by extra(ru.mipt.npm.gradle.KScienceVersions.ktorVersion)
val htmlVersion by extra(ru.mipt.npm.gradle.KScienceVersions.htmlVersion)
val kotlinWrappersVersion by extra("pre.148-kotlin-1.4.30")
val fxVersion by extra("14")

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://kotlin.bintray.com/kotlin-js-wrappers")
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
    }

    group = "hep.dataforge"
    version = "0.2.0-dev-5"
}

val githubProject by extra("visionforge")
val bintrayRepo by extra("dataforge")

subprojects {
    if (name.startsWith("visionforge")) {
        plugins.apply("ru.mipt.npm.gradle.publish")
    }
}

apiValidation {
    validationDisabled = true
    ignoredPackages.add("info.laht.threekt")
}