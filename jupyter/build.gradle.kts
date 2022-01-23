plugins {
    id("ru.mipt.npm.gradle.mpp")
    id("org.jetbrains.kotlin.jupyter.api")
}

description = "Common visionforge jupyter module"

kotlin {
    sourceSets {
        commonMain{
            dependencies{
                api(projects.visionforgeCore)
            }
        }
        jvmMain {
            dependencies {
                api(projects.visionforgeServer)
            }
        }
    }
}

readme {
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}