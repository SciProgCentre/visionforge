package space.kscience.visionforge.gdml.jupyter

import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.jupyter.JupyterPluginBase
import space.kscience.visionforge.solid.Solids

@DFExperimental
internal class GdmlForJupyter : JupyterPluginBase(
    Context("GDML") {
        plugin(Solids)
    }
) {

    override fun Builder.afterLoaded() {

        resources {
            js("three") {
                classPath("js/gdml-jupyter.js")
            }
        }

        import(
            "space.kscience.gdml.*",
            "space.kscience.visionforge.gdml.jupyter.*"
        )

        render<Gdml> { gdmlModel ->
            handler.produceHtml { vision(gdmlModel.toVision()) }
        }
    }
}
