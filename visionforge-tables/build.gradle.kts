plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.2.1"

kscience {
    jvm()
    js {
        useCommonJs()
        binaries.library()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    dependencies {
        api(projects.visionforgeCore)
        api("space.kscience:tables-kt:${tablesVersion}")
    }
    dependencies(jsMain) {
        implementation(npm("tabulator-tables", "5.5.2"))
        implementation(npm("@types/tabulator-tables", "5.5.3"))
    }
    useSerialization()
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}