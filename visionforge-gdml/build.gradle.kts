plugins {
    id("ru.mipt.npm.gradle.mpp")
}

kotlin {
    js{
        binaries.library()
    }
    sourceSets {
        commonMain{
            dependencies {
                api(projects.visionforgeSolid)
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