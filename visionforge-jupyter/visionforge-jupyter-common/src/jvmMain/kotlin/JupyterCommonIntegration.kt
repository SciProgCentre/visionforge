package space.kscience.visionforge.jupyter

import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.gdml.Gdml
import space.kscience.plotly.Plot
import space.kscience.plotly.PlotlyPlugin
import space.kscience.tables.Table
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.markup.MarkupPlugin
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.tables.TableVisionPlugin
import space.kscience.visionforge.tables.toVision
import space.kscience.visionforge.visionManager


public class JupyterCommonIntegration : VisionForgeIntegration(CONTEXT.visionManager) {

    override fun Builder.afterLoaded(vf: VisionForge) {

        resources {
            js("visionforge-common") {
                classPath("js/visionforge-jupyter-common.js")
            }
        }

        import(
            "space.kscience.gdml.*",
            "space.kscience.visionforge.solid.*",
            "space.kscience.tables.*",
            "space.kscience.dataforge.meta.*",
            "space.kscience.plotly.*",
            "space.kscience.plotly.models.*"
        )

        render<Gdml> { gdmlModel ->
            vf.produceHtml {
                vision { gdmlModel.toVision() }
            }
        }

        render<Table<*>> { table ->
            vf.produceHtml {
                vision { table.toVision() }
            }
        }

        render<Plot> { plot ->
            vf.produceHtml {
                vision { plot }
            }
        }

    }

    public companion object {
        private val CONTEXT: Context = Context("Jupyter-common") {
            plugin(Solids)
            plugin(PlotlyPlugin)
            plugin(TableVisionPlugin)
            plugin(MarkupPlugin)
        }
    }
}
