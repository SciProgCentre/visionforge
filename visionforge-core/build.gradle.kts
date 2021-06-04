plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra
val kotlinWrappersVersion: String by rootProject.extra

kscience{
    useSerialization()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("space.kscience:dataforge-context:$dataforgeVersion")
                api("org.jetbrains.kotlinx:kotlinx-html:${ru.mipt.npm.gradle.KScienceVersions.htmlVersion}")
                api("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-$kotlinWrappersVersion")
            }
        }
        jsMain {
            dependencies {
                api("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-$kotlinWrappersVersion")
            }
        }
    }
}