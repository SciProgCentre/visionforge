plugins {
    id("space.kscience.gradle.mpp")
}

kscience{
    jvm()
    js()
    dependencies {
        api(projects.visionforgeSolid)
    }
    useSerialization {
        json()
    }
}