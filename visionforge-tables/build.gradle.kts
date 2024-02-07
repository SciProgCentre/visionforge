plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.4.0"

kscience {
    jvm()
    js {
        binaries.library()
        browser {
            webpackTask{
                scssSupport {
                    enabled = true
                }
            }
        }
    }

    useSerialization()
    commonMain {
        api(projects.visionforgeCore)
        api("space.kscience:tables-kt:${tablesVersion}")
    }
    jsMain {
        api(npm("tabulator-tables", "5.5.2"))
        api(npm("@types/tabulator-tables", "5.5.3"))
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}