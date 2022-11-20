package space.kscience.visionforge.examples

import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.plotly.Plot
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.jupyter.VFIntegrationBase
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.asVision
import space.kscience.visionforge.solid.Solids

@DFExperimental
internal class VisionForgePlayGroundForJupyter : VFIntegrationBase(
    Context("VisionForge") {
        plugin(Solids)
        plugin(PlotlyPlugin)
    }
) {

    override fun Builder.afterLoaded() {
        resources {
            js("VisionForge") {
                classPath("js/visionforge-playground.js")
            }
        }

        import(
            "space.kscience.gdml.*",
            "space.kscience.plotly.*",
            "space.kscience.plotly.models.*",
            "space.kscience.visionforge.solid.*",
        )


        render<Gdml> { gdmlModel ->
            handler.produceHtml {
                vision { gdmlModel.toVision() }
            }
        }

        render<Plot> { plot ->
            handler.produceHtml {
                vision { plot.asVision() }
            }
        }
    }

}
