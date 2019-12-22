import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":dataforge-vis-common"))
            }
        }
        jvmMain {
            dependencies {
                implementation("org.fxyz3d:fxyz3d:0.5.2") {
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
                api(project(":wrappers"))
                implementation(npm("three", "0.106.2"))
                implementation(npm("@hi-level/three-csg", "1.0.6"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

