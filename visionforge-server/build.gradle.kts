plugins {
    id("space.kscience.gradle.jvm")
}

val ktorVersion = npmlibs.versions.ktor.get()

dependencies {
    api(project(":visionforge-core"))
    api("io.ktor:ktor-server-cio:${ktorVersion}")
    api("io.ktor:ktor-server-html-builder:${ktorVersion}")
    api("io.ktor:ktor-server-websockets:${ktorVersion}")
    implementation("io.ktor:ktor-server-cors:${ktorVersion}")
}