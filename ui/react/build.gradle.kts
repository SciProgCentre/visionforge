plugins {
    id("ru.mipt.npm.gradle.js")
}

val kotlinWrappersVersion: String by rootProject.extra

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.0-$kotlinWrappersVersion")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-$kotlinWrappersVersion")
//    implementation(npm("react-select","4.3.0"))
    implementation(project(":visionforge-threejs"))
}