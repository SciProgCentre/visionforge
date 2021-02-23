plugins {
    id("ru.mipt.npm.gradle.js")
}

val reactVersion by extra("17.0.1")
val kotlinWrappersVersion: String by rootProject.extra

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains:kotlin-styled:5.2.1-$kotlinWrappersVersion")
    api("org.jetbrains:kotlin-react-dom:$reactVersion-$kotlinWrappersVersion")
    implementation(project(":visionforge-threejs"))
}