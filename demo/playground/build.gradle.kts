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
                this.outputFileName = "js/visionforge-playground.js"
            }
            commonWebpackConfig {
                sourceMaps = true
                cssSupport.enabled = false
            }
        }
        binaries.executable()
    }

    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    afterEvaluate {
        val jsBrowserDistribution = tasks.getByName("jsBrowserDevelopmentExecutableDistribution")

        tasks.getByName<ProcessResources>("jvmProcessResources") {
            dependsOn(jsBrowserDistribution)
            afterEvaluate {
                from(jsBrowserDistribution)
            }
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":visionforge-solid"))
                api(project(":visionforge-gdml"))
                api(project(":visionforge-plotly"))
                api(projects.visionforge.visionforgeMarkdown)
                api(projects.visionforge.cernRootLoader)
            }
        }

        val jsMain by getting {
            dependencies {
                api(project(":ui:ring"))
                api(project(":visionforge-threejs"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(project(":visionforge-server"))
                api("ch.qos.logback:logback-classic:1.2.3")
                api("com.github.Ricky12Awesome:json-schema-serialization:0.6.6")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlinx.jupyter.api.plugin.tasks.JupyterApiResourcesTask> {
    libraryProducers = listOf("space.kscience.visionforge.examples.VisionForgePlayGroundForJupyter")
}

tasks.findByName("shadowJar")?.dependsOn("processJupyterApiResources")