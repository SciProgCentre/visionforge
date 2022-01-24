plugins {
    id("ru.mipt.npm.gradle.mpp")
}

kscience{
    useSerialization {
        json()
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":visionforge-solid"))
            }
        }
    }
}