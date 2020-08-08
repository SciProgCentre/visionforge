import scientifik.DependencyConfiguration
import scientifik.FXModule
import scientifik.useFx

plugins {
    id("scientifik.mpp")
    id("application")
}

val fxVersion: String by rootProject.extra
useFx(FXModule.CONTROLS, version = fxVersion, configuration = DependencyConfiguration.IMPLEMENTATION)

kotlin {

    jvm {
        withJava()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-solid"))
                api(project(":visionforge-gdml"))
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vis.solid.demo.FXDemoAppKt"
}