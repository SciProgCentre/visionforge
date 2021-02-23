package hep.dataforge.playground

import hep.dataforge.context.Context
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.gdml.gdml
import hep.dataforge.vision.html.Page
import hep.dataforge.vision.html.embedVisionFragment
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.html.scriptHeader
import hep.dataforge.vision.plotly.PlotlyPlugin
import hep.dataforge.vision.plotly.VisionOfPlotly
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.solid
import hep.dataforge.vision.visionManager
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kscience.plotly.Plot
import kscience.plotly.PlotlyFragment
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import space.kscience.gdml.Gdml

@JupyterLibrary
internal class VisionForgePlayGroundForJupyter : JupyterIntegration() {

    private val context = Context("Playground") {
        plugin(SolidManager)
        plugin(PlotlyPlugin)
    }

    val jsBundle = ResourceFallbacksBundle(listOf(ResourceLocation("js/visionforge-playground.js",
        ResourcePathType.CLASSPATH_PATH)))
    val jsResource = LibraryResource(name = "VisionForge", type = ResourceType.JS, bundles = listOf(jsBundle))

    override fun Builder.onLoaded(notebook: Notebook?) {
        resource(jsResource)

        import("space.kscience.gdml.*", "kscience.plotly.*", "kscience.plotly.models.*")

        onLoaded {
            val header = scriptHeader("js/visionforge-playground.js", null, hep.dataforge.vision.html.ResourceLocation.EMBED)
            display(HTML(createHTML().apply(header).finalize()))
        }

        render<Gdml> { gdmlModel ->
            val fragment = VisionManager.fragment {
                vision {
                    solid {
                        gdml(gdmlModel)
                    }
                }
            }

            val html = createHTML().div {
                embedVisionFragment(context.visionManager, fragment = fragment)
            }

            HTML(html)
        }

        render<Plot> { plot ->
            val fragment = VisionManager.fragment {
                vision {
                    VisionOfPlotly(plot.config)
                }
            }

            val html = createHTML().div {
                embedVisionFragment(context.visionManager, fragment = fragment)
            }

            HTML(html)
        }

        render<kscience.plotly.HtmlFragment> { fragment ->
            HTML(createHTML().apply(fragment.visit).finalize())
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }


    }

}
