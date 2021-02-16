plugins {
    id("ru.mipt.npm.js")
}

kotlin{
    js{
        binaries.library()
    }
}

dependencies {
    api(project(":visionforge-solid"))
    implementation(npm("three", "0.124.0"))
    implementation(npm("three-csg-ts", "2.2.0"))
}
