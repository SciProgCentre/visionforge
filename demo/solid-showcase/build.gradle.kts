import ru.mipt.npm.gradle.DependencyConfiguration
import ru.mipt.npm.gradle.FXModule

plugins {
    id("ru.mipt.npm.gradle.mpp")
    application
}

kscience {
    useCoroutines()
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
//                implementation(project(":visionforge-gdml"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project(":visionforge-fx"))
            }
        }
        jsMain {
            dependencies {
                implementation(project(":visionforge-threejs"))
            }
        }
    }
}

application {
    mainClassName = "space.kscience.visionforge.solid.demo.FXDemoAppKt"
}