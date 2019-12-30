import org.openjfx.gradle.JavaFXOptions
import scientifik.useSerialization

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
    id("application")
}

group = "ru.mipt.npm"

useSerialization()

val ktor_version = "1.3.0-rc"

kotlin {

    jvm {
        withJava()
    }

    js {
        browser {
            webpackTask {
                sourceMaps = true
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":dataforge-vis-spatial"))
            }
        }
        jvmMain{
            dependencies {
                implementation("org.apache.commons:commons-math3:3.6.1")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
            }
        }
    }
}

application {
    mainClassName = "ru.mipt.npm.muon.monitor.MMDemoAppKt"
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}