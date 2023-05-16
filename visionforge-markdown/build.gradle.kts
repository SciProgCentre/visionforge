plugins {
    id("space.kscience.gradle.mpp")
}

val markdownVersion = "0.4.1"

kscience {
    jvm()
    js {
        binaries.library()
    }
    dependencies {
        api(project(":visionforge-core"))
        api("org.jetbrains:markdown:$markdownVersion")
    }
    useSerialization()
}