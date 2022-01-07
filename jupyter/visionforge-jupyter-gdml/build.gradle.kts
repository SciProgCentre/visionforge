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
                implementation(projects.visionforgeSolid)
                implementation(projects.jupyter)
            }
        }
        jvmMain {
            dependencies {
                implementation(projects.visionforgeGdml)
            }
        }
        jsMain {
            dependencies {
                implementation(projects.visionforgeThreejs)
                implementation(projects.ui.ring)
            }
        }

    }
}

kscience {
    jupyterLibrary("space.kscience.visionforge.gdml.jupyter.GdmlForJupyter")
}

readme {
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}