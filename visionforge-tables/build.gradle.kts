plugins {
    id("space.kscience.gradle.mpp")
}

val tablesVersion = "0.3.0"

kscience {
    jvm()
    js {
        useCommonJs()
        binaries.library()
    }

    useSerialization()
    commonMain {
        api(projects.visionforgeCore)
        api("space.kscience:tables-kt:${tablesVersion}")
    }
    jsMain {
        implementation(npm("tabulator-tables", "5.5.2"))
        implementation(npm("@types/tabulator-tables", "5.5.3"))
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}