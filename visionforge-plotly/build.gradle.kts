plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val plotlyVersion = "0.5.0"

kscience {
    useSerialization()
}

kotlin {
    js {
        binaries.library()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
                api("space.kscience:plotlykt-core:${plotlyVersion}")
            }
        }
    }
}