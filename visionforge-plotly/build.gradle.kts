plugins {
    id("ru.mipt.npm.mpp")
}

kscience {
    useSerialization()
}

val plotlyVersion = "0.3.1-dev"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
                api("kscience.plotlykt:plotlykt-core:${plotlyVersion}")
            }
        }
    }
}