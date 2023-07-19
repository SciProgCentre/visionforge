package space.kscience.visionforge.jupyter

import kotlinx.html.*
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.plotly.Plot
import space.kscience.tables.*
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.markup.MarkupPlugin
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.asVision
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.tables.TableVisionPlugin
import space.kscience.visionforge.tables.toVision
import space.kscience.visionforge.visionManager


@DFExperimental
public class JupyterCommonIntegration : VisionForgeIntegration(CONTEXT.visionManager) {

    override fun Builder.afterLoaded() {

        resources {
            js("three") {
                classPath("js/visionforge-jupyter-common.js")
            }
        }

        import(
            "space.kscience.gdml.*",
            "space.kscience.visionforge.solid.*",
            "space.kscience.tables.*",
            "space.kscience.dataforge.meta.*",
        )

        render<Gdml> { gdmlModel ->
            handler.produceHtml {
                vision { gdmlModel.toVision() }
            }
        }

        render<Table<*>> { table ->
            handler.produceHtml {
                vision { table.toVision() }
            }
        }

        render<Plot> { plot ->
            handler.produceHtml {
                vision { plot.asVision() }
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
