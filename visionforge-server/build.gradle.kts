plugins {
    id("ru.mipt.npm.gradle.jvm")
}

dependencies {
    api(project(":visionforge-core"))
    api("io.ktor-server-cio:${npmlibs.versions.ktor}")
    api("io.ktor:ktor-server-html-builder:${npmlibs.versions.ktor}")
    api("io.ktor:ktor-server-websockets:${npmlibs.versions.ktor}")
}