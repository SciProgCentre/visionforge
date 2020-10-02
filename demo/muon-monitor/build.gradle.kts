import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

plugins {
    id("ru.mipt.npm.mpp")
    application
}

group = "ru.mipt.npm"

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

    js {
        useCommonJs()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
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
                implementation(project(":ui:bootstrap"))
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
            }
        }
    }
}

application {
    mainClassName = "ru.mipt.npm.muon.monitor.server.MMServerKt"
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}