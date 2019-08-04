plugins {
    id("scientifik.mpp")
}

scientifik{
    serialization = true
}

kotlin {
    jvm{
        withJava()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":dataforge-vis-common"))
            }
        }
        jvmMain{
            dependencies {

            }
        }
        jsMain{
            dependencies {
                api("info.laht.threekt:threejs-wrapper:0.106-npm-3")
                implementation(npm("three", "0.106.2"))
                implementation(npm("@hi-level/three-csg"))
                implementation(npm("style-loader"))
                implementation(npm("element-resize-event"))
            }
        }
    }
}

