plugins {
    id("ru.mipt.npm.gradle.mpp")
    kotlin("jupyter.api")
}

description = "Jupyter api artifact for GDML rendering"

kotlin{
    explicitApi = null
    js{
        browser {
            webpackTask {
                this.outputFileName = "js/gdml-jupyter.js"
            }
        }
        binaries.executable()
    }

    afterEvaluate {
        val jsBrowserDistribution by tasks.getting

        tasks.getByName<ProcessResources>("jvmProcessResources") {
            dependsOn(jsBrowserDistribution)
            afterEvaluate {
                from(jsBrowserDistribution)
            }
        }
    }

    sourceSets{
        commonMain {
            dependencies {
                api(project(":visionforge-solid"))
            }
        }
        jvmMain{
            dependencies {
                implementation(project(":visionforge-gdml"))
                implementation(kotlin("script-runtime"))
            }
        }
        jsMain {
            dependencies {
                api(project(":visionforge-threejs"))
                implementation(project(":ui:bootstrap"))
            }
        }

    }
}

readme{
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}