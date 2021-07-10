plugins {
    id("ru.mipt.npm.gradle.mpp")
}

description = "Jupyter api artifact for GDML rendering"

kotlin {
    explicitApi = null
    js {
        useCommonJs()
        browser {
            webpackTask {
                this.outputFileName = "js/gdml-jupyter.js"
            }
            commonWebpackConfig {
                sourceMaps = false
                cssSupport.enabled = false
            }
        }
        binaries.executable()
    }

    afterEvaluate {
        val jsBrowserDistribution by tasks.getting

        tasks.getByName<ProcessResources>("jvmProcessResources") {
            dependsOn(jsBrowserDistribution)
            from(jsBrowserDistribution)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-solid"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project(":visionforge-gdml"))
            }
        }
        jsMain {
            dependencies {
                api(project(":visionforge-threejs"))
                implementation(project(":ui:ring"))
            }
        }

    }
}

kscience {
    useJupyter()
}

readme {
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}