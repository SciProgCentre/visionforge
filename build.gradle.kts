val dataforgeVersion by extra("0.1.8-dev-2")

plugins {
    val toolsVersion = "0.5.0"
    id("scientifik.mpp") version toolsVersion apply false
    id("scientifik.jvm") version toolsVersion apply false
    id("scientifik.js") version toolsVersion apply false
    id("scientifik.publish") version toolsVersion apply false
}

allprojects {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/pdvrieze/maven")
        maven("http://maven.jzy3d.org/releases")
        maven("https://kotlin.bintray.com/js-externals")
        maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
        maven("https://dl.bintray.com/mipt-npm/dataforge")
//        maven("https://dl.bintray.com/gbaldeck/kotlin")
//        maven("https://dl.bintray.com/rjaros/kotlin")
    }

    group = "hep.dataforge"
    version = "0.1.4-dev"
}

val githubProject by extra("dataforge-vis")
val bintrayRepo by extra("dataforge")
val fxVersion by extra("14")

subprojects {
    if(name.startsWith("dataforge")) {
        apply(plugin = "scientifik.publish")
    }
    useSerialization()
    useFx(FXModule.CONTROLS, version = fxVersion)
}