plugins {
    id("ru.mipt.npm.gradle.js")
}

val kotlinWrappersVersion: String by rootProject.extra

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains:kotlin-styled:5.2.3-$kotlinWrappersVersion")
    api("org.jetbrains:kotlin-react-dom:17.0.2-$kotlinWrappersVersion")
    implementation(project(":visionforge-threejs"))
}