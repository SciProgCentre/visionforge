package hep.dataforge.playground

import hep.dataforge.context.Context
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.gdml.gdml
import hep.dataforge.vision.html.*
import hep.dataforge.vision.plotly.PlotlyPlugin
import hep.dataforge.vision.plotly.VisionOfPlotly
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.solid
import hep.dataforge.vision.visionManager
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import kscience.plotly.Plot
import kscience.plotly.PlotlyFragment
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import org.jetbrains.kotlinx.jupyter.api.libraries.ResourceLocation
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

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().div {
        val id = "visionforge.vision[${counter++}]"
        div {
            this.id = id
            embedVisionFragment(context.visionManager, fragment = fragment)
        }
        script {
            type = "text/javascript"
            unsafe { +"window.renderVisionsAt(\"$id\");" }
        }
    }

    override fun Builder.onLoaded(notebook: Notebook?) {
        resource(jsResource)

        import("space.kscience.gdml.*", "kscience.plotly.*", "kscience.plotly.models.*")

        render<Gdml> { gdmlModel ->
            val fragment = VisionManager.fragment {
                vision {
                    solid {
                        gdml(gdmlModel)
                    }
                }
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<Plot> { plot ->
            val fragment = VisionManager.fragment {
                vision {
                    VisionOfPlotly(plot.config)
                }
            }

            HTML( produceHtmlVisionString(fragment))
        }

        render<kscience.plotly.HtmlFragment> { fragment ->
            HTML(createHTML().apply(fragment.visit).finalize())
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }


    }

}
