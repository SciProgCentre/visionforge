import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

scientifik{
    withSerialization()
}

kotlin {
    jvm{
        withJava()
    }
    sourceSets {
        commonMain {
            dependencies {
                api(project(":dataforge-vis-common"))
            }
        }
        jvmMain{
            dependencies {
                implementation(project(":dataforge-vis-fx"))
                implementation("org.fxyz3d:fxyz3d:0.4.0")
            }
        }
        jsMain{
            dependencies {
                implementation(npm("three", "0.106.2"))
                implementation(npm("@hi-level/three-csg", "1.0.6"))
                implementation(npm("style-loader"))
                implementation(npm("element-resize-event"))
//                api("kotlin.js.externals:kotlin-js-jquery:3.2.0-0")
//                implementation(npm("jquery.fancytree","2.32.0"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

