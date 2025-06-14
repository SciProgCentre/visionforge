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
                api(compose.runtime)
            }
        }

        jsMain {
            dependencies {
                api("app.softwork:bootstrap-compose:0.3.1")
                api("app.softwork:bootstrap-compose-icons:0.3.0")
//                implementation(npm("bootstrap", "5.3.3"))
//                implementation(npm(" bootstrap-icons", "1.11.3"))
                api(compose.html.core)
            }
        }
    }
}