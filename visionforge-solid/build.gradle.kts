plugins {
    id("ru.mipt.npm.gradle.mpp")
}

kscience{
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