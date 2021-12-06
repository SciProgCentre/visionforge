plugins {
    id("ru.mipt.npm.gradle.mpp")
    id("org.jetbrains.kotlin.jupyter.api")
}

description = "Common visionforge jupyter module"

kotlin {
    sourceSets {
        commonMain{
            dependencies{
                api(projects.visionforge.visionforgeCore)
            }
        }
        jvmMain {
            dependencies {
                implementation(project(":visionforge-server"))
            }
        }
    }
}

readme {
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}