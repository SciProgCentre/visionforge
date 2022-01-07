plugins {
    id("ru.mipt.npm.gradle.project")
    id("org.jetbrains.kotlinx.kover") version "0.5.0-RC"
}

val dataforgeVersion by extra("0.5.2")
val fxVersion by extra("11")

subprojects {
    if (name.startsWith("visionforge")) apply<MavenPublishPlugin>()

    repositories {
        maven("https://repo.kotlin.link")
        mavenCentral()
        maven("https://maven.jzy3d.org/releases")
    }

    group = "space.kscience"
    version = "0.2.0-dev-99"

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

//workaround for https://youtrack.jetbrains.com/issue/KT-48273
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.apply {
        webpack.version = "5.64.3"
        webpackDevServer.version = "4.5.0"
    }
}