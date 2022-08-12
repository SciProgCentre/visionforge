plugins {
    id("space.kscience.gradle.mpp")
}

val plotlyVersion = "0.5.3-dev-1"

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