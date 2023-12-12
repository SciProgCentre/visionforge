plugins {
    id("space.kscience.gradle.mpp")
}

kscience{
    jvm()
    useKtor()
    jvmMain {
        api(projects.visionforgeCore)
        api("io.ktor:ktor-server-host-common")
        api("io.ktor:ktor-server-html-builder")
        api("io.ktor:ktor-server-websockets")
        api("io.ktor:ktor-server-cors")
    }
}

