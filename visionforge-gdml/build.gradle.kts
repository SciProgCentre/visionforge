plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    jvm()
    js {
        binaries.library()
    }
    dependencies {
        api(projects.visionforgeSolid)
        api("space.kscience:gdml:0.5.0")
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}