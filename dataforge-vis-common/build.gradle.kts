import org.openjfx.gradle.JavaFXOptions

plugins {
    id("scientifik.mpp")
    id("org.openjfx.javafxplugin")
}

scientifik{
    withSerialization()
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    jvm{
        withJava()
    }

    sourceSets {
        commonMain{
            dependencies {
                api("hep.dataforge:dataforge-output:$dataforgeVersion")
            }
        }
        jvmMain{
            dependencies {
                api("no.tornado:tornadofx:1.7.19")
                api("no.tornado:tornadofx-controlsfx:0.1")
            }
        }
        jsMain{
            dependencies {
                api("hep.dataforge:dataforge-output-html:$dataforgeVersion")
                api(npm("text-encoding"))
                api("org.jetbrains:kotlin-extensions:1.0.1-pre.83-kotlin-1.3.50")
                api(npm("core-js"))
            }
        }
    }
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}
