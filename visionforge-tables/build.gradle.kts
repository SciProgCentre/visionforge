plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.2.0-dev-4"

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
        implementation(npm("tabulator-tables", "5.4.4"))
        implementation(npm("@types/tabulator-tables", "5.4.8"))
    }
    useSerialization()
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}