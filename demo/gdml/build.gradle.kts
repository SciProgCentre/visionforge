import scientifik.DependencyConfiguration
import scientifik.FXModule
import scientifik.fx

plugins {
    id("scientifik.mpp")
    id("application")
}

val fxVersion: String by rootProject.extra
fx(FXModule.CONTROLS, version = fxVersion, configuration = DependencyConfiguration.IMPLEMENTATION)

kotlin {

    jvm {
        withJava()
    }

    js {
        browser {
            webpackTask {
                //sourceMaps = false
            }
        }
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
    mainClassName = "hep.dataforge.vis.spatial.gdml.demo.GDMLDemoAppKt"
}

val convertGdmlToJson by tasks.creating(JavaExec::class) {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    main = "hep.dataforge.vis.spatial.gdml.demo.SaveToJsonKt"
}