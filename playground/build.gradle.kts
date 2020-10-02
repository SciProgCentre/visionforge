plugins {
    kotlin("multiplatform")
}

repositories{
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/mipt-npm/dataforge")
    maven("https://dl.bintray.com/mipt-npm/kscience")
    maven("https://dl.bintray.com/mipt-npm/dev")
}

kotlin {
    js(IR) {
        browser {}
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-solid"))
                api(project(":visionforge-gdml"))
                api(project(":ui:bootstrap"))
            }
        }
    }
}
