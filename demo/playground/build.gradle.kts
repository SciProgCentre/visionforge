plugins {
    kotlin("multiplatform")
    kotlin("jupyter.api")
    id("com.github.johnrengelman.shadow") version "7.1.2"
//    application
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.kotlin.link")
}

kotlin {

    js(IR) {
        browser {
            webpackTask {
                cssSupport{
                    enabled = true
                }
                scssSupport{
                    enabled = true
                }
                mainOutputFileName.set("js/visionforge-playground.js")
            }
        }
        binaries.executable()
    }

    jvm {
//        withJava()
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs =
                    freeCompilerArgs + "-Xjvm-default=all" + "-Xopt-in=kotlin.RequiresOptIn" + "-Xlambdas=indy" + "-Xcontext-receivers"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.visionforgeSolid)
                implementation(projects.visionforgePlotly)
                implementation(projects.visionforgeMarkdown)
                implementation(projects.visionforgeTables)
                implementation(projects.cernRootLoader)
                api(projects.visionforgeJupyter.visionforgeJupyterCommon)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(projects.visionforgeThreejs)
                compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-cio:${spclibs.versions.ktor.get()}")
                implementation(projects.visionforgeGdml)
                implementation(projects.visionforgeServer)
                implementation(spclibs.logback.classic)
                implementation("com.github.Ricky12Awesome:json-schema-serialization:0.6.6")
            }
        }
        all {
            languageSettings.optIn("space.kscience.dataforge.misc.DFExperimental")
        }
    }
}

val jsBrowserDistribution = tasks.getByName("jsBrowserDistribution")

tasks.getByName<ProcessResources>("jvmProcessResources") {
    dependsOn(jsBrowserDistribution)
    from(jsBrowserDistribution) {
        exclude("**/*.js.map")
    }
}

val processJupyterApiResources by tasks.getting(org.jetbrains.kotlinx.jupyter.api.plugin.tasks.JupyterApiResourcesTask::class) {
    libraryProducers = listOf("space.kscience.visionforge.examples.VisionForgePlayGroundForJupyter")
}

tasks.findByName("shadowJar")?.dependsOn(processJupyterApiResources)

//application{
//    mainClass.set("space.kscience.visionforge.examples.ShapesKt")
//}