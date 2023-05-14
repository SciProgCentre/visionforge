plugins {
    id("space.kscience.gradle.js")
}

kotlin{
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled
    js{
        binaries.library()
    }
}

dependencies {
    api(projects.visionforgeSolid)
    implementation(npm("three", "0.143.0"))
    implementation(npm("three-csg-ts", "3.1.10"))
    implementation(npm("three.meshline","1.4.0"))
}
