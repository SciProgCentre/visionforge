//import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

plugins {
    id("ru.mipt.npm.jvm")
}

val ktorVersion: String by rootProject.extra

dependencies {
    api(project(":visionforge-core"))
    api("io.ktor:ktor-server-cio:$ktorVersion")
    //api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-html-builder:$ktorVersion")
    api("io.ktor:ktor-websockets:$ktorVersion")
}