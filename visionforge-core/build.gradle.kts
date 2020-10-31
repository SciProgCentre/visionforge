plugins {
    id("ru.mipt.npm.mpp")
}

val dataforgeVersion: String by rootProject.extra
val kotlinWrappersVersion: String by rootProject.extra


kotlin {

    sourceSets {
        commonMain {
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
        jvmMain {
            dependencies {
                api("no.tornado:tornadofx:1.7.20")
                //api("no.tornado:tornadofx-controlsfx:0.1.1")
                api("de.jensd:fontawesomefx-fontawesome:4.7.0-11") {
                    exclude(group = "org.openjfx")
                }
                api("de.jensd:fontawesomefx-commons:11.0") {
                    exclude(group = "org.openjfx")
                }
            }
        }
        jsMain {
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                api("org.jetbrains:kotlin-styled:5.2.0-$kotlinWrappersVersion")
                api("org.jetbrains:kotlin-extensions:1.0.1-$kotlinWrappersVersion")
            }
        }
    }
}