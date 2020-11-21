plugins {
    id("ru.mipt.npm.mpp")
}

val dataforgeVersion: String by rootProject.extra
val kotlinWrappersVersion: String by rootProject.extra
val htmlVersion: String by rootProject.extra


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("hep.dataforge:dataforge-context:$dataforgeVersion")
                api("org.jetbrains.kotlinx:kotlinx-html:$htmlVersion")
            }
        }
        jsMain {
            dependencies {
                api("org.jetbrains:kotlin-extensions:1.0.1-$kotlinWrappersVersion")
                api("org.jetbrains:kotlin-css:1.0.0-$kotlinWrappersVersion")
            }
        }
    }
}