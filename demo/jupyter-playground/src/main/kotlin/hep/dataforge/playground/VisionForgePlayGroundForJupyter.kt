package hep.dataforge.playground

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.Page
import hep.dataforge.vision.html.embedVisionFragment
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.plotly.toVision
import hep.dataforge.vision.plotly.withPlotly
import hep.dataforge.vision.solid.withSolids
import hep.dataforge.vision.visionManager
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import kscience.plotly.Plot
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.Notebook
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import space.kscience.gdml.Gdml

@JupyterLibrary
@DFExperimental
internal class VisionForgePlayGroundForJupyter : JupyterIntegration() {

    val jsBundle = ResourceFallbacksBundle(listOf(ResourceLocation("js/visionforge-playground.js",
        ResourcePathType.CLASSPATH_PATH)))
    val jsResource = LibraryResource(name = "VisionForge", type = ResourceType.JS, bundles = listOf(jsBundle))

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().div {
        val id = "visionforge.vision[${counter++}]"
        div {
            this.id = id
            embedVisionFragment(VisionForge.visionManager, fragment = fragment)
        }
        script {
            type = "text/javascript"
            unsafe { +"window.renderVisionsAt(\"$id\");" }
        }
    }

    override fun Builder.onLoaded(notebook: Notebook?) {
        resource(jsResource)

        onLoaded {
            VisionForge.withPlotly().withSolids()
        }

        import("space.kscience.gdml.*",
            "kscience.plotly.*",
            "kscience.plotly.models.*",
            "kotlinx.html.*",
            "hep.dataforge.vision.solid.*")
        import("hep.dataforge.vision.VisionForge")
        render<Gdml> { gdmlModel ->
            val fragment = VisionManager.fragment {
                vision(gdmlModel.toVision())
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<Vision> { vision ->
            val fragment = VisionManager.fragment {
                vision(vision)
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<Plot> { plot ->
            val fragment = VisionManager.fragment {
                vision(plot.toVision())
            }

            HTML(produceHtmlVisionString(fragment))
        }

        render<kscience.plotly.HtmlFragment> { fragment ->
            HTML(createHTML().apply(fragment.visit).finalize())
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }
    }

}
