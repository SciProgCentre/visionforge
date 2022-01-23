plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val tablesVersion = "0.1.4"

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
    maturity = ru.mipt.npm.gradle.Maturity.PROTOTYPE
}