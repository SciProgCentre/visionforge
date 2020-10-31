import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

plugins {
    id("ru.mipt.npm.mpp")
    application
}

val ktorVersion: String by rootProject.extra

kscience {
    application()
}

kotlin {
    afterEvaluate {
        val jsBrowserDistribution by tasks.getting

        jvm {
            withJava()
            compilations[MAIN_COMPILATION_NAME]?.apply {
                tasks.getByName<ProcessResources>(processResourcesTaskName) {
                    dependsOn(jsBrowserDistribution)
                    afterEvaluate {
                        from(jsBrowserDistribution)
                    }
                }
            }

        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
            }
        }
        jvmMain {
            dependencies {
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
            }
        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
            }
        }
    }
}

application {
    mainClass.set("ru.mipt.npm.muon.monitor.server.MMServerKt")
}

//distributions {
//    main {
//        contents {
//            from("$buildDir/libs") {
//                rename("${rootProject.name}-jvm", rootProject.name)
//                into("lib")
//            }
//        }
//    }
//}