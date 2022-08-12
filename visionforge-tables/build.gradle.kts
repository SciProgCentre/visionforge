plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.2.0-dev-3"

kscience {
    useSerialization()
}

kotlin {
    js {
        useCommonJs()
        binaries.library()
        browser{
            commonWebpackConfig{
                cssSupport.enabled = true
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
                api("space.kscience:tables-kt:${tablesVersion}")
            }
        }
        jsMain {
            dependencies {
                implementation(npm("tabulator-tables", "5.0.1"))
                implementation(npm("@types/tabulator-tables", "5.0.1"))
            }
        }
    }
}

readme{
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}