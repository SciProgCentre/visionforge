plugins {
    `npm-multiplatform`
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
                api("hep.dataforge:dataforge-output-metadata:$dataforgeVersion")
            }
        }
        val jvmMain by getting{
            dependencies {
                api("hep.dataforge:dataforge-output-jvm:$dataforgeVersion")
            }
        }
        val jsMain by getting{
            dependencies {
                api("hep.dataforge:dataforge-output-js:$dataforgeVersion")
            }
        }
    }
}