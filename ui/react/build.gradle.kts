plugins {
    id("ru.mipt.npm.js")
}

val reactVersion by extra("17.0.0")
val kotlinWrappersVersion: String by rootProject.extra

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains:kotlin-styled:5.2.0-$kotlinWrappersVersion")
    api("org.jetbrains:kotlin-react-dom:$reactVersion-$kotlinWrappersVersion")
    implementation(project(":visionforge-threejs"))
}