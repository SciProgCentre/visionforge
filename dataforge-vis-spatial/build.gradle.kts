plugins {
    `npm-multiplatform`
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":dataforge-vis-common"))
            }
        }
    }
}

