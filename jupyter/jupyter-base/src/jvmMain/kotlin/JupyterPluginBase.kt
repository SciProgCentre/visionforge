package space.kscience.visionforge.jupyter

import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.Vision
import space.kscience.visionforge.html.HtmlFormFragment
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.fragment

@DFExperimental
public abstract class JupyterPluginBase(final override val context: Context) : JupyterIntegration(), ContextAware {

    protected val handler: VisionForgeServerHandler = VisionForgeServerHandler(context)

    protected abstract fun Builder.afterLoaded()

    final override fun Builder.onLoaded() {

        onLoaded {
            declare("visionForge" to handler)
        }

        onShutdown {
            handler.stopServer()
        }

        import(
            "kotlinx.html.*",
            "space.kscience.visionforge.html.*"
        )


        render<HtmlVisionFragment> { fragment ->
            handler.produceHtml(fragment = fragment)
        }

        render<Vision> { vision ->
            handler.produceHtml {
                vision(vision)
            }

        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
        }

        render<HtmlFormFragment> { fragment ->
            handler.produceHtml {
                fragment(fragment.formBody)
                vision(fragment.vision)
            }
        }
        afterLoaded()
    }
}
