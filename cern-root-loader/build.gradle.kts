plugins {
    id("space.kscience.gradle.mpp")
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