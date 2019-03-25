plugins {
    kotlin("multiplatform")
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    jvm()
    js()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
                //api("hep.dataforge:dataforge-output-metadata:$dataforgeVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output-jvm:$dataforgeVersion")
                //api("no.tornado:tornadofx:1.7.18")
            }
        }
        val jsMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output-js:$dataforgeVersion")
            }
        }
    }
}