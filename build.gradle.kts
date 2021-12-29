plugins {
    id("ru.mipt.npm.gradle.project")
}

val dataforgeVersion by extra("0.5.2")
val fxVersion by extra("11")

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.kotlin.link")
        maven("https://maven.jzy3d.org/releases")
    }

    group = "space.kscience"
    version = "0.2.0"
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


//workaround for https://youtrack.jetbrains.com/issue/KT-48273
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.apply {
        webpack.version = "5.64.3"
        webpackDevServer.version = "4.5.0"
    }
}