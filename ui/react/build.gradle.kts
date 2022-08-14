plugins {
    id("space.kscience.gradle.js")
}

dependencies{
    api(project(":visionforge-solid"))
    api("org.jetbrains.kotlin-wrappers:kotlin-styled")
    api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
//    implementation(npm("react-select","4.3.0"))
    implementation(project(":visionforge-threejs"))
}