plugins {
    id("space.kscience.gradle.jvm")
}

kscience{
    useKtor()
    dependencies {
        api(projects.visionforgeCore)
        api("io.ktor:ktor-server-host-common")
        api("io.ktor:ktor-server-html-builder")
        api("io.ktor:ktor-server-websockets")
        implementation("io.ktor:ktor-server-cors")
    }
}

