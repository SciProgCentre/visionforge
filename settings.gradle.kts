pluginManagement {
    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/mipt-npm/dataforge")
        maven("https://dl.bintray.com/mipt-npm/scientifik")
        maven("https://dl.bintray.com/mipt-npm/dev")
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "kotlinx-atomicfu" -> useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
                "kotlin-multiplatform" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.jvm" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "kotlin-dce-js" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.frontend" -> useModule("org.jetbrains.kotlin:kotlin-frontend-plugin:${requested.version}")
                "scientifik.mpp", "scientifik.publish", "scientifik.jvm", "scientifik.js" -> useModule("scientifik:gradle-tools:${requested.version}")
                "org.openjfx.javafxplugin" -> useModule("org.openjfx:javafx-plugin:${requested.version}")
            }
        }
    }
}

enableFeaturePreview("GRADLE_METADATA")

rootProject.name = "dataforge-vis"

include(
    ":dataforge-vis-common",
    ":wrappers",
    ":dataforge-vis-fx",
    ":dataforge-vis-spatial",
    ":dataforge-vis-spatial-gdml",
    ":spatial-js-demo"
)

//if(file("../dataforge-core").exists()) {
//    includeBuild("../dataforge-core"){
//        dependencySubstitution {
//            //substitute(module("hep.dataforge:dataforge-output")).with(project(":dataforge-output"))
//            substitute(module("hep.dataforge:dataforge-output-jvm")).with(project(":dataforge-output"))
//            substitute(module("hep.dataforge:dataforge-output-js")).with(project(":dataforge-output"))
//        }
//    }
//}