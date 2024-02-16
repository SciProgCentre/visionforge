plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
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

        jsMain{
            dependencies {
                api("app.softwork:bootstrap-compose:0.1.15")
                api("app.softwork:bootstrap-compose-icons:0.1.15")

                api(compose.html.core)
            }
        }
    }
}