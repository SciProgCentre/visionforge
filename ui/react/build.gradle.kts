plugins {
    id("ru.mipt.npm.js")
}

val reactVersion by extra("17.0.0")
val kotlinWrappersVersion: String by rootProject.extra

dependencies{
    api(project(":visionforge-core"))
    api("org.jetbrains:kotlin-react-dom:$reactVersion-$kotlinWrappersVersion")
}