plugins {
    `npm-multiplatform`
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
    }
}