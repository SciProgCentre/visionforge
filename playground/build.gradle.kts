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
    jvm()
    js(IR) {
        browser {
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
                implementation(project(":visionforge-gdml"))

            }
        }
        val jsMain by getting{
            dependencies {
                implementation(project(":ui:bootstrap"))
            }
        }

        val jvmMain by getting{
            dependencies {
                implementation("com.github.Ricky12Awesome:json-schema-serialization:0.6.6")
            }
        }
    }
}
