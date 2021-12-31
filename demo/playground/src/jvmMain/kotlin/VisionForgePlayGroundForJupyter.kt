package space.kscience.visionforge.examples

import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.plotly.Plot
import space.kscience.visionforge.Vision
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.embedAndRenderVisionFragment
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.asVision
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.visionManager

@DFExperimental
public class VisionForgePlayGroundForJupyter : JupyterIntegration() {

    private val context = Context("VisionForge") {
        plugin(Solids)
        plugin(PlotlyPlugin)
    }

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().apply {
        embedAndRenderVisionFragment(context.visionManager, counter++, fragment = fragment)
    }.finalize()

    override fun Builder.onLoaded() {

        resources {
            js("VisionForge"){
                classPath("js/visionforge-playground.js")
            }
        }

        import(
            "space.kscience.gdml.*",
            "space.kscience.plotly.*",
            "space.kscience.plotly.models.*",
            "kotlinx.html.*",
            "space.kscience.visionforge.solid.*",
            "space.kscience.visionforge.html.Page",
            "space.kscience.visionforge.html.page"
        )

        render<Gdml> { gdmlModel ->
            val fragment = HtmlVisionFragment {
                vision(gdmlModel.toVision())
            }
            HTML(produceHtmlVisionString(fragment))
        }

        render<Vision> { vision ->
            val fragment = HtmlVisionFragment {
                vision(vision)
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<Plot> { plot ->
            val fragment = HtmlVisionFragment {
                vision(plot.asVision())
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<space.kscience.plotly.PlotlyHtmlFragment> { fragment ->
            HTML(createHTML().apply(fragment.visit).finalize())
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }
    }

}
