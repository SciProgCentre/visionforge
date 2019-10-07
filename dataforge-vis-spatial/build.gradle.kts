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
                implementation(project(":dataforge-vis-fx"))
                implementation("org.fxyz3d:fxyz3d:0.5.2")
            }
        }
        jsMain {
            dependencies {
                implementation(npm("three", "0.106.2"))
                implementation(npm("@hi-level/three-csg", "1.0.6"))
                implementation(npm("style-loader"))
                implementation(npm("inspire-tree","6.0.1"))
                implementation(npm("inspire-tree-dom","4.0.6"))
                implementation(npm("jsoneditor"))
//                api("org.jetbrains:kotlin-extensions:1.0.1-pre.83-kotlin-1.3.50")
//                api(npm("core-js"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

