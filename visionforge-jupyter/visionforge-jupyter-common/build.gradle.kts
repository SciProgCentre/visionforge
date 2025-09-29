plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.kotlin.jupyter.api)
}

description = "Jupyter api artifact including all common modules"

kscience {
    fullStack(
        "js/visionforge-jupyter-common.js",
        browserConfig = {
            webpackTask {
                cssSupport{
                    enabled = true
                }
                scssSupport {
                    enabled = true
                }
            }
        }
    )
    dependencies {
        api(projects.visionforgeSolid)
        api(projects.plotlyKt.plotlyKtCore)
        api(projects.visionforgeTables)
        api(projects.visionforgeMarkdown)
        api(projects.visionforgeJupyter)
    }

    jvmMain {
        api(projects.visionforgeGdml)
    }

    jsMain {
        implementation(projects.visionforgeThreejs)
    }
}


//tasks.processJupyterApiResources {
//    libraryProducers = listOf("space.kscience.visionforge.jupyter.JupyterCommonIntegration")
//}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}