import ru.mipt.npm.gradle.DependencyConfiguration
import ru.mipt.npm.gradle.FXModule

plugins {
    id("ru.mipt.npm.gradle.mpp")
    application
}

kscience {
    val fxVersion: String by rootProject.extra
    useFx(FXModule.CONTROLS, version = fxVersion, configuration = DependencyConfiguration.IMPLEMENTATION)
    application()
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
                implementation(project(":visionforge-gdml"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project(":visionforge-fx"))
            }
        }
        jsMain {
            dependencies {
                implementation(project(":ui:bootstrap"))
                implementation(project(":visionforge-threejs"))
                implementation(npm("react-file-drop", "3.0.6"))
            }
        }
    }
}

application {
    mainClass.set("space.kscience.visionforge.gdml.demo.GdmlFxDemoAppKt")
}

val convertGdmlToJson by tasks.creating(JavaExec::class) {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    main = "space.kscience.dataforge.vis.spatial.gdml.demo.SaveToJsonKt"
}