plugins {
    kotlin("multiplatform")
    alias(spclibs.plugins.kotlin.jupyter.api)
    id("com.gradleup.shadow") version "8.3.6"

}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.kotlin.link")
}

kotlin {
    jvmToolchain(17)
    js {
        useEsModules()
        browser {
            commonWebpackConfig {
                outputFileName = "js/visionforge-playground.js"
                cssSupport {
                    enabled = true
                }
                scssSupport {
                    enabled = true
                }
            }
        }
        binaries.executable()
    }

    jvm {
//        withJava()
        compilerOptions {
            freeCompilerArgs.addAll("-Xjvm-default=all", "-Xcontext-parameters")

        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.visionforgeSolid)
                implementation(projects.plotlyKt.plotlyKtCore)
                implementation(projects.visionforgeMarkdown)
                implementation(projects.visionforgeTables)
                implementation(projects.cernRootLoader)
                api(projects.visionforgeJupyter.visionforgeJupyterCommon)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(projects.visionforgeThreejs)
//                compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-cio")
                implementation(projects.visionforgeGdml)
                implementation(projects.visionforgeServer)
                implementation(spclibs.logback.classic)
            }
        }

        all {
            languageSettings.optIn("space.kscience.dataforge.misc.DFExperimental")
        }
    }
}

val debug = false

val jsBrowserDistribution = if(debug) {
    tasks.getByName("jsBrowserDevelopmentExecutableDistribution")
} else {
    tasks.getByName("jsBrowserDistribution")
}

tasks.getByName<ProcessResources>("jvmProcessResources") {
    dependsOn(jsBrowserDistribution)
    from(jsBrowserDistribution)
}

val processJupyterApiResources by tasks.getting(org.jetbrains.kotlinx.jupyter.api.plugin.tasks.JupyterApiResourcesTask::class) {
    libraryProducers = listOf("space.kscience.visionforge.examples.VisionForgePlayGroundForJupyter")
}

tasks.findByName("shadowJar")?.dependsOn(processJupyterApiResources)

//application{
//    mainClass.set("space.kscience.visionforge.examples.ShapesKt")
//}