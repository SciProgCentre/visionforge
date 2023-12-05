
plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
//    id("org.jetbrains.compose") version "1.5.11"
//    id("com.android.library")
}

kscience{
    jvm()
    js()
//    wasm()
}

kotlin {
//    android()
    sourceSets {
        val commonMain by getting {
            dependencies {

            }
        }

        val jvmMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.preview)
            }
        }

        val jsMain by getting{
            dependencies {
                api(compose.html.core)
                api("app.softwork:bootstrap-compose:0.1.15")
                api("app.softwork:bootstrap-compose-icons:0.1.15")
                api(projects.visionforge.visionforgeThreejs)
            }
        }
    }
}