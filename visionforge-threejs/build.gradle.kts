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
    implementation(npm("three", "0.137.5"))
    implementation(npm("three-csg-ts", "3.1.10"))
}
