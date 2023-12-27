plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

kscience {
    jvm()
    js()
//    wasm()
}

kotlin {
//    android()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.visionforgeCore)
                api(compose.runtime)
            }
        }

        val jvmMain by getting {
            dependencies {
                api(compose.foundation)
                api(compose.material)
                api(compose.preview)
            }
        }

        val jsMain by getting {
            dependencies {
                api(compose.html.core)
                api("app.softwork:bootstrap-compose:0.1.15")
                api("app.softwork:bootstrap-compose-icons:0.1.15")
            }
        }
    }
}