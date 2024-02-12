plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

kscience {
    jvm()
//    wasm()
}

kotlin {
//    android()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.visionforgeCore)
                api(compose.runtime)
                api(compose.foundation)
            }
        }
        jvmMain{
            dependencies{
                api(compose.material)
                api(compose.preview)
            }
        }
    }
}