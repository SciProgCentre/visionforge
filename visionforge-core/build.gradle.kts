plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra

kscience{
    useSerialization()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("space.kscience:dataforge-context:$dataforgeVersion")
                api("org.jetbrains.kotlinx:kotlinx-html:${ru.mipt.npm.gradle.KScienceVersions.htmlVersion}")
                api("org.jetbrains.kotlin-wrappers:kotlin-css")
            }
        }
        jsMain {
            dependencies {
                api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
            }
        }
    }
}