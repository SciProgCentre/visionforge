plugins {
    id("ru.mipt.npm.gradle.jvm")
}

dependencies {
    api(project(":visionforge-core"))
    api(npmlibs.ktor.server.cio)
    api(npmlibs.ktor.html.builder)
    api(npmlibs.ktor.websockets)
}