import scientifik.useSerialization

plugins {
    id("scientifik.mpp")
}

useSerialization()

kotlin {
    js {
        useCommonJs()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
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
                implementation(project(":ui:bootstrap"))//to be removed later
                implementation(npm("three", "0.114.0"))
                implementation(npm("three-csg-ts", "1.0.1"))
                api(npm("file-saver", "2.0.2"))
            }
        }
    }
}
