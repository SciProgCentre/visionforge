plugins {
    id("ru.mipt.npm.gradle.jvm")
}

val ktorVersion: String by rootProject.extra

dependencies {
    api(project(":visionforge-core"))
    api("io.ktor:ktor-server-cio:$ktorVersion")
    //api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-html-builder:$ktorVersion")
    api("io.ktor:ktor-websockets:$ktorVersion")
}