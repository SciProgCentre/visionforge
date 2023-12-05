plugins {
    id("space.kscience.gradle.mpp")
}

val plotlyVersion = "0.6.1"

kscience {
    jvm()
    js {
        binaries.library()
    }
    dependencies {
        api(projects.visionforgeCore)
        api("space.kscience:plotlykt-core:${plotlyVersion}")
    }
    useSerialization()
}