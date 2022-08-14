plugins {
    id("space.kscience.gradle.js")
}

kotlin{
    js{
        binaries.library()
    }
}

dependencies {
    api(project(":visionforge-solid"))
    implementation(npm("three", "0.143.0"))
    implementation(npm("three-csg-ts", "3.1.10"))
    implementation(npm("three.meshline","1.4.0"))
}
