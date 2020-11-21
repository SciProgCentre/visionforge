plugins {
    id("ru.mipt.npm.mpp")
}

val kvisionVersion: String = "3.16.2"

kscience{
    application()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
            }
        }
        jsMain {
            dependencies {
                implementation(project(":visionforge-threejs"))
            }
        }
    }
}