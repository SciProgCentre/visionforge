package space.kscience.visionforge.gdml.jupyter

import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.visionforge.Vision
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.embedAndRenderVisionFragment
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.visionManager

@JupyterLibrary
@DFExperimental
internal class GdmlForJupyter : JupyterIntegration() {

    private val context = Context("GDML") {
        plugin(Solids)
    }

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().apply {
        embedAndRenderVisionFragment(context.visionManager, counter++, fragment)
    }.finalize()

    override fun Builder.onLoaded() {

        resources {
            js("three") {
                classPath("js/gdml-jupyter.js")
            }
//            css("override") {
//                classPath("css/jupyter-override.css")
//            }
        }

        import(
            "space.kscience.gdml.*",
            "kotlinx.html.*",
            "space.kscience.visionforge.solid.*",
            "space.kscience.visionforge.html.Page",
            "space.kscience.visionforge.html.page",
            "space.kscience.visionforge.gdml.jupyter.*"
        )

        render<Vision> { vision ->
            HTML(produceHtmlVisionString { vision(vision) })
        }

        render<Gdml> { gdmlModel ->
            HTML(produceHtmlVisionString { vision(gdmlModel.toVision()) })
        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }
    }
}
