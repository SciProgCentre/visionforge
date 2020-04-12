import scientifik.serialization

plugins {
    id("scientifik.mpp")
}

serialization()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":dataforge-vis-common"))
            }
        }
        jvmMain {
            dependencies {
                api("org.fxyz3d:fxyz3d:0.5.2") {
                    exclude(module = "slf4j-simple")
                }
                api("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${Scientifik.coroutinesVersion}")
                implementation("eu.mihosoft.vrl.jcsg:jcsg:0.5.7") {
                    exclude(module = "slf4j-simple")
                }
            }
        }
        jsMain {
            dependencies {
//                api(project(":wrappers"))
                implementation(npm("three", "0.114.0"))
                implementation(npm("three-csg-ts", "1.0.1"))
            }
        }
    }
}
