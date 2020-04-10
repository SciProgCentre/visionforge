import scientifik.jsDistDirectory

plugins {
    id("scientifik.mpp")
    id("application")
}

group = "ru.mipt.npm"

val ktorVersion = "1.3.2"

kotlin {

    val installJS = tasks.getByName("jsBrowserDistribution")

    jvm {
        withJava()
        compilations.findByName("main").apply {
            tasks.getByName<ProcessResources>("jvmProcessResources") {
                dependsOn(installJS)
                afterEvaluate {
                    from(project.jsDistDirectory)
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":dataforge-vis-spatial"))
            }
        }
        jvmMain {
            dependencies {
                implementation("org.apache.commons:commons-math3:3.6.1")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
            }
        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
                implementation(npm("text-encoding"))
                implementation(npm("abort-controller"))
                implementation(npm("bufferutil"))
                implementation(npm("utf-8-validate"))
                implementation(npm("fs"))
//                implementation(npm("jquery"))
//                implementation(npm("popper.js"))
//                implementation(npm("react-is"))
            }
        }
    }
}

application {
    mainClassName = "ru.mipt.npm.muon.monitor.server.MMServerKt"
}

//configure<JavaFXOptions> {
//    modules("javafx.controls")
//}