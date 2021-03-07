plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra
val kotlinWrappersVersion: String by rootProject.extra
val htmlVersion: String by rootProject.extra

kscience{
    useSerialization()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("space.kscience:dataforge-context:$dataforgeVersion")
                api("org.jetbrains.kotlinx:kotlinx-html:$htmlVersion")
                api("org.jetbrains:kotlin-css:1.0.0-$kotlinWrappersVersion")
            }
        }
        jsMain {
            dependencies {
                api("org.jetbrains:kotlin-extensions:1.0.1-$kotlinWrappersVersion")
            }
        }
    }
}