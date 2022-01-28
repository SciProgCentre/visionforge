plugins {
    id("ru.mipt.npm.gradle.js")
}

kotlin{
    js{
        binaries.library()
    }
}

dependencies {
    api(project(":visionforge-solid"))
    implementation(npm("three", "0.137.4"))
    implementation(npm("three-csg-ts", "3.1.9"))
}
