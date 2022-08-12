plugins {
    id("space.kscience.gradle.mpp")
}

kotlin {
    js(IR){
        binaries.library()
    }
    sourceSets {
        commonMain{
            dependencies {
                api(projects.visionforgeSolid)
                api("space.kscience:gdml:0.4.0")
            }
        }
        jvmTest{
            dependencies{
                implementation("ch.qos.logback:logback-classic:1.2.11")
            }
        }
    }
}