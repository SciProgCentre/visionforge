import org.openjfx.gradle.JavaFXOptions
import scientifik.useSerialization

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

val dataforgeVersion: String by rootProject.extra
//val kvisionVersion: String by rootProject.extra("2.0.0-M1")

useSerialization()

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
                //api("no.tornado:tornadofx-controlsfx:0.1.1")
                api("de.jensd:fontawesomefx-fontawesome:4.7.0-11")
                api("de.jensd:fontawesomefx-commons:11.0")
            }
        }
        jsMain{
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                api(npm("bootstrap","4.4.1"))
                implementation(npm("jsoneditor"))
                implementation(npm("file-saver"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}
