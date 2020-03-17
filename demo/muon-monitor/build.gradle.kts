import scientifik.jsDistDirectory

plugins {
    id("scientifik.mpp")
    id("application")
}

group = "ru.mipt.npm"

val ktor_version = "1.3.2"

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
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-serialization:$ktor_version")
            }
        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation("io.ktor:ktor-client-serialization-js:$ktor_version")
                implementation(npm("text-encoding"))
                implementation(npm("abort-controller"))
            }
        }
    }
}

application {
    mainClassName = "ru.mipt.npm.muon.monitor.server/MMServerKt"
}

//configure<JavaFXOptions> {
//    modules("javafx.controls")
//}