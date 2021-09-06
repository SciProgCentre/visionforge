plugins {
    kotlin("multiplatform")
}

repositories{
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/mipt-npm/dataforge")
    maven("https://dl.bintray.com/mipt-npm/kscience")
    maven("https://dl.bintray.com/mipt-npm/dev")
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

    jvm{
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    afterEvaluate {
        val jsBrowserDistribution  = tasks.getByName("jsBrowserDevelopmentExecutableDistribution")

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

        val jsMain by getting{
            dependencies {
                api(project(":ui:ring"))
                api(project(":visionforge-threejs"))
            }
        }

        val jvmMain by getting{
            dependencies {
                api(project(":visionforge-server"))
                api("ch.qos.logback:logback-classic:1.2.3")
                api("com.github.Ricky12Awesome:json-schema-serialization:0.6.6")
            }
        }
    }
}
