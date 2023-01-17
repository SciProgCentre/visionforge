plugins {
    kotlin("multiplatform")
    kotlin("jupyter.api")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.kotlin.link")
}

kotlin {

    js(IR) {
        useCommonJs()
        browser {
            webpackTask {
                outputFileName = "js/visionforge-playground.js"
            }
            commonWebpackConfig {
                sourceMaps = true
                cssSupport{
                    enabled.set(false)
                }
            }
        }
        binaries.executable()
    }

    jvm {
        withJava()
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
                implementation(projects.visionforgeGdml)
                implementation(projects.visionforgePlotly)
                implementation(projects.visionforgeMarkdown)
                implementation(projects.visionforgeTables)
                implementation(projects.cernRootLoader)
                implementation(projects.jupyter)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(projects.ui.ring)
                implementation(projects.visionforgeThreejs)
                compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(projects.visionforgeServer)
                implementation("ch.qos.logback:logback-classic:1.2.3")
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