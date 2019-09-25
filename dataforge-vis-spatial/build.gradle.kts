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
                implementation("org.fxyz3d:fxyz3d:0.4.0")
            }
        }
        jsMain {
            dependencies {
                implementation(npm("three", "0.106.2"))
                implementation(npm("@hi-level/three-csg", "1.0.6"))
                implementation(npm("style-loader"))
                implementation(npm("element-resize-event"))
                implementation(npm("inspire-tree","6.0.1"))
                implementation(npm("inspire-tree-dom","4.0.6"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

