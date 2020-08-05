plugins {
    id("scientifik.mpp")
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    js {
        useCommonJs()
    }

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
                api("org.jetbrains.kotlinx:kotlinx-html:0.6.12")

                //api("org.jetbrains:kotlin-extensions:1.0.1-pre.105-kotlin-1.3.72")
                //api("org.jetbrains:kotlin-css-js:1.0.0-pre.105-kotlin-1.3.72")
                api("org.jetbrains:kotlin-styled:1.0.0-pre.104-kotlin-1.3.72")

                api(npm("core-js", "2.6.5"))
                api(npm("inline-style-prefixer", "5.1.0"))
                api(npm("styled-components", "4.3.2"))
                //api(project(":ringui-wrapper"))
            }
        }
    }
}