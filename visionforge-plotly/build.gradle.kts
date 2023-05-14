plugins {
    id("space.kscience.gradle.mpp")
}

val plotlyVersion = "0.5.3"

kscience {
    jvm()
    js {
        binaries.library()
    }
    dependencies {
        api(project(":visionforge-core"))
        api("space.kscience:plotlykt-core:${plotlyVersion}")
    }
    useSerialization()
}