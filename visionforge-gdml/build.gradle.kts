plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    jvm()
    js {
        binaries.library()
    }
//    native()
//    wasmJs()
    dependencies {
        api(projects.visionforgeSolid)
        api(libs.gdml)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    // TODO remove into a separate library
    maturity = space.kscience.gradle.Maturity.DEPRECATED
}