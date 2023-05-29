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
        api(projects.visionforgeCore)
        api("org.jetbrains:markdown:$markdownVersion")
        api("org.jetbrains:annotations:24.0.0")
    }
    useSerialization()
}