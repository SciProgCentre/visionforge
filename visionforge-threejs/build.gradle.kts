plugins {
    id("ru.mipt.npm.gradle.js")
}

dependencies {
    api(project(":visionforge-solid"))
    implementation(npm("three", "0.130.1"))
    implementation(npm("three-csg-ts", "3.1.6"))
}
