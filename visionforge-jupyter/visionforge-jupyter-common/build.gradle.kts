plugins {
    id("space.kscience.gradle.mpp")
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
        api(projects.visionforgePlotly)
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

    jupyterLibrary("space.kscience.visionforge.jupyter.JupyterCommonIntegration")
}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}