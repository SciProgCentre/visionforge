plugins {
    id("scientifik.mpp")
}

scientifik{
    withSerialization()
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
                api("org.jetbrains:kotlin-extensions:1.0.1-pre.83-kotlin-1.3.50")
                api(npm("core-js"))
            }
        }
    }
}