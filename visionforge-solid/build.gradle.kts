plugins {
    id("ru.mipt.npm.mpp")
}

kscience {
    useSerialization()
}

kotlin {

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
            }
        }
    }
}