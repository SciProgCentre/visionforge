plugins {
    id("scientifik.mpp")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                api("scientifik:gdml:0.1.4-dev-1")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
            }
        }
    }
}