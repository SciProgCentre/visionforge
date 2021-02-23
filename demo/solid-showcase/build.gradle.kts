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

val kotlinWrappersVersion: String by rootProject.extra

kotlin {

    jvm {
        afterEvaluate {
            withJava()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
//                implementation(project(":visionforge-gdml"))
            }
        }
        jvmMain{
            dependencies{
                implementation(project(":visionforge-fx"))
            }
        }
        jsMain{
            dependencies {
                implementation(project(":visionforge-threejs"))
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vision.solid.demo.FXDemoAppKt"
}