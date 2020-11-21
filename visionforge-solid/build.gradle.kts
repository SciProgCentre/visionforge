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
        jsMain {
            dependencies {
                implementation(npm("three", "0.122.0"))
                implementation(npm("three-csg-ts", "1.0.1"))
            }
        }
    }
}