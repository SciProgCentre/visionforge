plugins {
    id("scientifik.mpp")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                api("scientifik:gdml:0.1.7")
            }
        }
    }
}

//tasks{
//    val jsBrowserWebpack by getting(KotlinWebpack::class) {
//        sourceMaps = false
//    }
//}