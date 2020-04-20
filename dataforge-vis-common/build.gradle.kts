plugins {
    id("scientifik.mpp")
}

val dataforgeVersion: String by rootProject.extra
//val kvisionVersion: String by rootProject.extra("2.0.0-M1")

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

                //React, React DOM + Wrappers (chapter 3)
                api("org.jetbrains:kotlin-react:16.13.0-pre.94-kotlin-1.3.70")
                api("org.jetbrains:kotlin-react-dom:16.13.0-pre.94-kotlin-1.3.70")
                api(npm("react", "16.13.0"))
                api(npm("react-dom", "16.13.0"))

                //Kotlin Styled (chapter 3)
                api("org.jetbrains:kotlin-styled:1.0.0-pre.94-kotlin-1.3.70")
                api(npm("styled-components"))
                api(npm("inline-style-prefixer"))

                api(npm("source-map-resolve","0.6.0"))
                api(npm("file-saver","2.0.2"))
            }
        }
    }
}