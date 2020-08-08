import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import scientifik.jsDistDirectory

plugins {
    id("scientifik.mpp")
    id("application")
}

group = "ru.mipt.npm"

val ktorVersion = "1.3.2"

kotlin {

    val installJS = tasks.getByName("jsBrowserDistribution")

    js {
        browser {
            dceTask {
                dceOptions {
                    keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
                }
            }
            webpackTask {
                mode = org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.Mode.PRODUCTION
            }
        }
    }

    jvm {
        withJava()
        compilations[MAIN_COMPILATION_NAME]?.apply {
            tasks.getByName<ProcessResources>(processResourcesTaskName) {
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