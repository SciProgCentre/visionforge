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

    js {
        useCommonJs()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
                implementation(project(":visionforge-gdml"))
            }
        }
        jsMain{
            dependencies {
                implementation(project(":ui:bootstrap"))
                implementation(npm("react-file-drop", "3.0.6"))
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