plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

kscience {
    js()
    jvm()
}

kotlin {
//    android()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.visionforgeCore)
                //need this to placate compose compiler in MPP applications
                api(libs.compose.runtime)
            }
        }

        jsMain {
            dependencies {
                api(libs.bootstrap.compose)
                api(libs.bootstrap.compose.icons)
                api(libs.compose.html.core)
            }
        }
    }
}