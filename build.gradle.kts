plugins {
    id("ru.mipt.npm.gradle.project")
//    id("org.jetbrains.kotlinx.kover") version "0.5.0"
}

val dataforgeVersion by extra("0.6.0-dev-12")
val fxVersion by extra("11")

allprojects{
    group = "space.kscience"
    version = "0.3.0-dev-2"
}

subprojects {
    if (name.startsWith("visionforge")) apply<MavenPublishPlugin>()

    repositories {
        maven("https://repo.kotlin.link")
        mavenCentral()
        maven("https://maven.jzy3d.org/releases")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>{
        kotlinOptions{
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
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