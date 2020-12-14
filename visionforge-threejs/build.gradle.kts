plugins {
    id("ru.mipt.npm.js")
}

kscience {
    useSerialization()
}

dependencies {
    api(project(":visionforge-solid"))
    implementation(npm("three", "0.122.0"))
    implementation(npm("three-csg-ts", "2.0.0"))
}
