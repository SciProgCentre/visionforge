plugins {
    id("ru.mipt.npm.js")
}

val dataforgeVersion: String by rootProject.extra

dependencies {
    api(project(":visionforge-solid"))
    api(project(":ui:react"))
    implementation(npm("file-saver", "2.0.2"))
}