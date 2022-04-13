plugins {
    id("ru.mipt.npm.gradle.mpp")
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
    }
}