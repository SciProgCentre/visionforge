plugins {
    id("ru.mipt.npm.gradle.js")
}

val dataforgeVersion: String by rootProject.extra

dependencies {
    api(project(":visionforge-solid"))
    api(project(":ui:react"))
    implementation(npm("file-saver", "2.0.2"))
    implementation(npm("bootstrap","4.6.0"))
    implementation(npm("jquery","3.5.1"))
    implementation(npm("popper.js","1.16.1"))
}