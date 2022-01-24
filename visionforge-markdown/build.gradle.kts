plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val markdownVersion = "0.2.4"

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
                api("org.jetbrains:markdown:$markdownVersion")
            }
        }
    }
}