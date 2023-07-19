plugins {
    id("space.kscience.gradle.mpp")
}

description = "Jupyter api artifact including all common modules"

kscience {
    fullStack(
        "js/visionforge-jupyter-common.js",
        jsConfig = { useCommonJs() }
    ) {
        commonWebpackConfig {
            sourceMaps = false
            cssSupport {
                enabled.set(false)
            }
        }
    }

    dependencies {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgePlotly)
        implementation(projects.visionforgeTables)
        implementation(projects.visionforgeMarkdown)
        implementation(projects.visionforgeJupyter)
    }

    jvmMain {
        implementation(projects.visionforgeGdml)
    }

    jsMain {
        implementation(projects.ui.ring)
        implementation(projects.visionforgeThreejs)
    }

    jupyterLibrary("space.kscience.visionforge.jupyter.JupyterCommonIntegration")
}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}