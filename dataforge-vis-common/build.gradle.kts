import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

val dataforgeVersion: String by rootProject.extra
//val kvisionVersion: String by rootProject.extra("2.0.0-M1")

kotlin {
    jvm{
        withJava()
    }

    sourceSets {
        commonMain{
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
        jvmMain{
            dependencies {
                api("no.tornado:tornadofx:1.7.19")
                api("no.tornado:tornadofx-controlsfx:0.1")
            }
        }
        jsMain{
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                api(npm("bootstrap","4.4.1"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}
