import ru.mipt.npm.gradle.*

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
                api(project(":visionforge-solid"))
                api(project(":visionforge-gdml"))
            }
        }
    }
}

application {
    mainClassName = "hep.dataforge.vision.solid.demo.FXDemoAppKt"
}