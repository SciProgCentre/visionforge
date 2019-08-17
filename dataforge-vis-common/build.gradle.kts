plugins {
    id("scientifik.mpp")
}

scientifik{
    serialization = true
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                api(npm("text-encoding"))
            }
        }
    }
}