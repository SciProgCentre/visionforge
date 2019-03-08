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
                api("hep.dataforge:dataforge-io:$dataforgeVersion")
                api("hep.dataforge:dataforge-io-metadata:$dataforgeVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-io-jvm:$dataforgeVersion")
                //api("no.tornado:tornadofx:1.7.18")
            }
        }
        val jsMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-io-js:$dataforgeVersion")
            }
        }
    }
}