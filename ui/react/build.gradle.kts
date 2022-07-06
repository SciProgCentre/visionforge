plugins {
    id("ru.mipt.npm.gradle.js")
}

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains.kotlin-wrappers:kotlin-styled")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
//    implementation(npm("react-select","4.3.0"))
    implementation(project(":visionforge-threejs"))
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    versions.webpackCli.version = "4.10.0"
}