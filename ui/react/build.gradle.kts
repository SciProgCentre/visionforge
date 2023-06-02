plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    js()
    jsMain {
        dependencies {
            api(projects.visionforgeSolid)
            api("org.jetbrains.kotlin-wrappers:kotlin-styled")
            api("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
//    implementation(npm("react-select","4.3.0"))
            implementation(projects.visionforgeThreejs)
        }
    }
}