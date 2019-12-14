import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

scientifik {
    withSerialization()
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
                api("org.fxyz3d:fxyz3d:0.5.2")
                implementation("eu.mihosoft.vrl.jcsg:jcsg:0.5.7")
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

