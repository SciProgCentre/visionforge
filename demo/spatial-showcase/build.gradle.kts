import ru.mipt.npm.gradle.DependencyConfiguration
import ru.mipt.npm.gradle.FXModule
import ru.mipt.npm.gradle.useFx

plugins {
    id("ru.mipt.npm.mpp")
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
        afterEvaluate {
            withJava()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
                implementation(project(":visionforge-gdml"))
            }
        }
        jvmMain{
            dependencies{
                implementation(project(":visionforge-fx"))
            }
        }
        jsMain{
            dependencies {
                implementation("org.jetbrains:kotlin-css:1.0.0-pre.129-kotlin-1.4.10")
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vision.solid.demo.FXDemoAppKt"
}