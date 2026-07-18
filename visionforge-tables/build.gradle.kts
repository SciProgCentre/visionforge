plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.4.1"

kscience {
    jvm()
    js {
        binaries.library()
        browser {
            webpackTask {
                cssSupport {
                    enabled = true
                }
                scssSupport {
                    enabled = true
                }
            }
        }
    }
    native()
    wasmJs()

    useSerialization()
    commonMain {
        api(projects.visionforgeCore)
        api(libs.dataforge.tables)
    }
    jsMain {
        api("org.jetbrains.kotlin-wrappers:kotlin-js")
        implementation(npm("tabulator-tables", "6.3.1"))
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}