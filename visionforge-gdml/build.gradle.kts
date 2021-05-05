plugins {
    kotlin("multiplatform")
    id("ru.mipt.npm.gradle.mpp")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":visionforge-solid"))
                api("space.kscience:gdml:0.4.0")
            }
        }
    }
}

//tasks{
//    val jsBrowserWebpack by getting(KotlinWebpack::class) {
//        sourceMaps = false
//    }
//}