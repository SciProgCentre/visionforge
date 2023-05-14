plugins {
    id("space.kscience.gradle.mpp")
}

description = "Jupyter api artifact for GDML rendering"

kscience {
    fullStack("js/gdml-jupyter.js",
        jsConfig = { useCommonJs() }
    ) {
        commonWebpackConfig {
            sourceMaps = false
            cssSupport {
                enabled.set(false)
            }
        }
    }

    dependencies{
        implementation(projects.visionforgeSolid)
        implementation(projects.jupyter)
    }

    dependencies(jvmMain){
        implementation(projects.visionforgeGdml)
    }

    dependencies(jsMain){
        implementation(projects.visionforgeThreejs)
        implementation(projects.ui.ring)
    }
    
    jupyterLibrary("space.kscience.visionforge.gdml.jupyter.GdmlForJupyter")
}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}