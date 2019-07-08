plugins {
    id("scientifik.mpp")
}

repositories{
    maven("https://dl.bintray.com/pdvrieze/maven")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                api("scientifik:gdml:0.1.1")
            }
        }
    }
}