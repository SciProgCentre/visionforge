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
        api("space.kscience:gdml:0.4.0")
    }
    dependencies(jvmTest) {
        implementation("ch.qos.logback:logback-classic:1.2.11")
    }
}