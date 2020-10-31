import ru.mipt.npm.gradle.useFx

plugins {
    id("ru.mipt.npm.project")
}

val dataforgeVersion by extra("0.2.0-dev-4")
val ktorVersion by extra("1.4.1")
val kotlinWrappersVersion by extra("pre.129-kotlin-1.4.10")

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
val fxVersion by extra("14")

subprojects {
    if(name.startsWith("visionforge")) {
        apply<ru.mipt.npm.gradle.KSciencePublishPlugin>()
    }
    afterEvaluate {
        extensions.findByType<ru.mipt.npm.gradle.KScienceExtension>()?.run {
            useSerialization()
            useFx(ru.mipt.npm.gradle.FXModule.CONTROLS, version = fxVersion)
        }
    }
}

apiValidation {
    validationDisabled = true
    ignoredPackages.add("info.laht.threekt")
}