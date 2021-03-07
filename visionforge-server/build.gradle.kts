plugins {
    id("ru.mipt.npm.gradle.jvm")
}

val ktorVersion = ru.mipt.npm.gradle.KScienceVersions.ktorVersion

dependencies {
    api(project(":visionforge-core"))
    api("io.ktor:ktor-server-cio:$ktorVersion")
    //api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-html-builder:$ktorVersion")
    api("io.ktor:ktor-websockets:$ktorVersion")
}