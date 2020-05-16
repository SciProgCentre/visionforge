import scientifik.*

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
                api(project(":dataforge-vis-spatial"))
                api(project(":dataforge-vis-spatial-gdml"))
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vis.spatial.demo.FXDemoAppKt"
}