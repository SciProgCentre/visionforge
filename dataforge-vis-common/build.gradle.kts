import scientifik.serialization

plugins {
    id("scientifik.mpp")
}

val dataforgeVersion: String by rootProject.extra
//val kvisionVersion: String by rootProject.extra("2.0.0-M1")

serialization()
val fxVersion: String by rootProject.extra

kotlin {
    sourceSets {
        commonMain{
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
        jvmMain{
            dependencies {
                api("no.tornado:tornadofx:1.7.20")
                //api("no.tornado:tornadofx-controlsfx:0.1.1")
                api("de.jensd:fontawesomefx-fontawesome:4.7.0-11"){
                    exclude(group = "org.openjfx")
                }
                api("de.jensd:fontawesomefx-commons:11.0"){
                    exclude(group = "org.openjfx")
                }
            }
        }
        jsMain{
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                //api(npm("bootstrap","4.4.1"))
                implementation(npm("uri-js","4.2.2"))
                implementation(npm("jsoneditor","8.6.1"))
                implementation(npm("file-saver"))
            }
        }
    }
}