plugins {
    id("ru.mipt.npm.gradle.project")
    id("org.jetbrains.kotlinx.kover") version "0.5.0-RC"
}

val dataforgeVersion by extra("0.5.2")
val fxVersion by extra("11")

allprojects{
    group = "space.kscience"
    version = "0.2.0-dev-99"
}

subprojects {
    if (name.startsWith("visionforge")) apply<MavenPublishPlugin>()

    repositories {
        maven("https://repo.kotlin.link")
        mavenCentral()
        maven("https://maven.jzy3d.org/releases")
    }
}

ksciencePublish {
    github("visionforge")
    space()
    sonatype()
}

apiValidation {
    ignoredPackages.add("info.laht.threekt")
}

readme.readmeTemplate = file("docs/templates/README-TEMPLATE.md")