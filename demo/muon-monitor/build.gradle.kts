plugins {
    id("space.kscience.gradle.mpp")
    application
}

group = "ru.mipt.npm"

val ktorVersion: String = npmlibs.versions.ktor.get()

kscience {
    useCoroutines()
    useSerialization()
    application()
}

kotlin {
    jvm {
        withJava()
    }
    js {
        useCommonJs()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(false)
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
                implementation("org.apache.commons:commons-math3:3.6.1")
                implementation("io.ktor:ktor-server-cio:${ktorVersion}")
                implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
                implementation("ch.qos.logback:logback-classic:1.2.11")
            }
        }
        jsMain {
            dependencies {
                implementation(project(":ui:ring"))
                implementation(project(":visionforge-threejs"))
                //implementation(devNpm("webpack-bundle-analyzer", "4.4.0"))
            }
        }
    }
}

application {
    mainClass.set("ru.mipt.npm.muon.monitor.server.MMServerKt")
}

val jsBrowserDistribution by tasks.getting
val jsBrowserDevelopmentExecutableDistribution by tasks.getting

val devMode = rootProject.findProperty("visionforge.development") as? Boolean
    ?: rootProject.version.toString().contains("dev")

tasks.getByName<ProcessResources>("jvmProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    if (devMode) {
        dependsOn(jsBrowserDevelopmentExecutableDistribution)
        from(jsBrowserDevelopmentExecutableDistribution)
    } else {
        dependsOn(jsBrowserDistribution)
        from(jsBrowserDistribution)
    }
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