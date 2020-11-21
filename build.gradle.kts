plugins {
    id("ru.mipt.npm.project")
}

val dataforgeVersion by extra("0.2.0-dev-8")
val ktorVersion by extra("1.4.2")
val htmlVersion by extra("0.7.2")
val kotlinWrappersVersion by extra("pre.129-kotlin-1.4.10")
val fxVersion by extra("14")


allprojects {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
    }

    group = "hep.dataforge"
    version = "0.2.0-dev-2"
}

val githubProject by extra("visionforge")
val bintrayRepo by extra("dataforge")

subprojects {
    if(name.startsWith("visionforge")) {
        apply<ru.mipt.npm.gradle.KSciencePublishPlugin>()
    }
}

apiValidation {
    validationDisabled = true
    ignoredPackages.add("info.laht.threekt")
}