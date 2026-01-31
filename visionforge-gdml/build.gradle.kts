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
        api("space.kscience:gdml:0.5.0")
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    // TODO remove into a separate library
    maturity = space.kscience.gradle.Maturity.DEPRECATED
}